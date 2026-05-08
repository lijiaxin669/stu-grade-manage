package com.example.grade.appeal.service;

import com.example.grade.appeal.domain.AppealStatus;
import com.example.grade.appeal.domain.AppealStateMachine;
import com.example.grade.appeal.domain.GradeAppeal;
import com.example.grade.appeal.domain.AppealAuditLog;
import com.example.grade.appeal.dto.*;
import com.example.grade.appeal.repository.AppealAuditLogRepository;
import com.example.grade.appeal.repository.GradeAppealRepository;
import com.example.grade.common.ErrorCode;
import com.example.grade.domain.Course;
import com.example.grade.domain.Enrollment;
import com.example.grade.domain.Grade;
import com.example.grade.exception.BusinessException;
import com.example.grade.repository.CourseRepository;
import com.example.grade.repository.EnrollmentRepository;
import com.example.grade.repository.GradeRepository;
import com.example.grade.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppealService {

    private final GradeAppealRepository gradeAppealRepository;
    private final AppealAuditLogRepository appealAuditLogRepository;
    private final GradeRepository gradeRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final AppealAuditService appealAuditService;

    @Transactional
    public AppealDto createAppeal(CustomUserDetails user, AppealCreateDto dto) {
        Long studentId = user.getUserId();

        Grade grade = gradeRepository.findById(dto.getGradeId())
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "成绩记录不存在"));

        Enrollment enrollment = grade.getEnrollment();
        if (!enrollment.getStudent().getId().equals(studentId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只能申诉自己的成绩");
        }

        if (grade.getUpdatedAt().plusDays(7).isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "只能在成绩更新后7天内发起申诉");
        }

        BigDecimal originalScore = grade.getScore();
        BigDecimal expectedScore = dto.getExpectedScore();
        if (originalScore.subtract(expectedScore).abs().compareTo(BigDecimal.valueOf(2)) < 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "期望分数与原分数差异必须≥2");
        }

        if (gradeAppealRepository.existsByGradeIdAndStatusNot(dto.getGradeId(), AppealStatus.CLOSED)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "该成绩已存在未关闭的申诉");
        }

        GradeAppeal appeal = new GradeAppeal();
        appeal.setGradeId(dto.getGradeId());
        appeal.setStudentId(studentId);
        appeal.setCourseId(enrollment.getCourse().getId());
        appeal.setOriginalScore(originalScore);
        appeal.setExpectedScore(expectedScore);
        appeal.setReason(dto.getReason());
        appeal.setStatus(AppealStatus.PENDING);
        appeal.setCreatedAt(LocalDateTime.now());
        appeal.setUpdatedAt(LocalDateTime.now());
        gradeAppealRepository.save(appeal);

        appealAuditService.writeAuditLog(appeal.getId(), null, AppealStatus.PENDING.name(), studentId, "ROLE_STUDENT", "学生发起申诉", true, null);

        return toDto(appeal, enrollment);
    }

    @Transactional(readOnly = true)
    public List<AppealDto> getMyAppeals(CustomUserDetails user) {
        List<GradeAppeal> appeals = gradeAppealRepository.findByStudentId(user.getUserId());
        return appeals.stream().map(a -> {
            Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseId(a.getStudentId(), a.getCourseId()).orElse(null);
            return toDto(a, enrollment);
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AppealDto> getPendingAppeals(CustomUserDetails user) {
        List<Long> courseIds = courseRepository.findByTeacherId(user.getUserId())
            .stream().map(Course::getId).collect(Collectors.toList());
        if (courseIds.isEmpty()) return List.of();
        List<GradeAppeal> appeals = gradeAppealRepository.findByCourseIdInAndStatusIn(
            courseIds, List.of(AppealStatus.PENDING, AppealStatus.UNDER_REVIEW));
        return appeals.stream().map(a -> {
            Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseId(a.getStudentId(), a.getCourseId()).orElse(null);
            return toDto(a, enrollment);
        }).collect(Collectors.toList());
    }

    @Transactional
    public AppealDto claimAppeal(CustomUserDetails user, Long appealId) {
        GradeAppeal appeal = gradeAppealRepository.findByIdForUpdate(appealId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "申诉不存在"));

        if (appeal.getStatus() != AppealStatus.PENDING) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "该申诉已被认领");
        }

        Course course = courseRepository.findById(appeal.getCourseId())
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "课程不存在"));
        if (!course.getTeacher().getId().equals(user.getUserId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只能认领自己课程下的申诉");
        }

        AppealStatus from = appeal.getStatus();
        AppealStateMachine.validate(from, AppealStatus.UNDER_REVIEW);
        appeal.setStatus(AppealStatus.UNDER_REVIEW);
        appeal.setCurrentHandlerId(user.getUserId());
        appeal.setUpdatedAt(LocalDateTime.now());
        gradeAppealRepository.save(appeal);

        appealAuditService.writeAuditLog(appealId, from.name(), AppealStatus.UNDER_REVIEW.name(), user.getUserId(), "ROLE_TEACHER", "教师认领申诉", true, null);

        Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseId(appeal.getStudentId(), appeal.getCourseId()).orElse(null);
        return toDto(appeal, enrollment);
    }

    @Transactional
    public AppealDto decision(CustomUserDetails user, Long appealId, AppealDecisionDto dto) {
        GradeAppeal appeal = gradeAppealRepository.findByIdForUpdate(appealId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "申诉不存在"));

        if (appeal.getStatus() != AppealStatus.UNDER_REVIEW) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "只能对审核中的申诉做出裁定");
        }

        Course course = courseRepository.findById(appeal.getCourseId())
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "课程不存在"));
        if (!course.getTeacher().getId().equals(user.getUserId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只能裁定自己课程下的申诉");
        }

        AppealStatus targetStatus;
        if ("APPROVED".equalsIgnoreCase(dto.getDecision())) {
            targetStatus = AppealStatus.APPROVED;
            if (dto.getFinalScore() == null) {
                throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "通过裁定须填写最终分数");
            }
        } else if ("REJECTED".equalsIgnoreCase(dto.getDecision())) {
            targetStatus = AppealStatus.REJECTED;
        } else {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "decision必须为APPROVED或REJECTED");
        }

        AppealStatus from = appeal.getStatus();
        AppealStateMachine.validate(from, targetStatus);

        appeal.setStatus(targetStatus);
        appeal.setTeacherComment(dto.getComment());
        if (targetStatus == AppealStatus.APPROVED) {
            appeal.setFinalScore(dto.getFinalScore());
        }
        appeal.setUpdatedAt(LocalDateTime.now());
        gradeAppealRepository.save(appeal);

        appealAuditService.writeAuditLog(appealId, from.name(), targetStatus.name(), user.getUserId(), "ROLE_TEACHER", dto.getComment(), true, null);

        if (targetStatus == AppealStatus.APPROVED) {
            eventPublisher.publishEvent(new AppealApprovedEvent(appeal.getId(), appeal.getGradeId(), appeal.getFinalScore()));
        }

        Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseId(appeal.getStudentId(), appeal.getCourseId()).orElse(null);
        return toDto(appeal, enrollment);
    }

    @Transactional
    public AppealDto requestArbitration(CustomUserDetails user, Long appealId, ArbitrateRequestDto dto) {
        GradeAppeal appeal = gradeAppealRepository.findByIdForUpdate(appealId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "申诉不存在"));

        if (!appeal.getStudentId().equals(user.getUserId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只能对自己的申诉发起仲裁");
        }

        if (appeal.getStatus() != AppealStatus.REJECTED) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "只能对已驳回的申诉发起仲裁");
        }

        if (appeal.getUpdatedAt().plusDays(3).isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "驳回后3天内才能发起仲裁");
        }

        AppealStatus from = appeal.getStatus();
        AppealStateMachine.validate(from, AppealStatus.ARBITRATING);
        appeal.setStatus(AppealStatus.ARBITRATING);
        appeal.setCurrentHandlerId(null);
        appeal.setUpdatedAt(LocalDateTime.now());
        gradeAppealRepository.save(appeal);

        appealAuditService.writeAuditLog(appealId, from.name(), AppealStatus.ARBITRATING.name(), user.getUserId(), "ROLE_STUDENT", dto.getReason(), true, null);

        Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseId(appeal.getStudentId(), appeal.getCourseId()).orElse(null);
        return toDto(appeal, enrollment);
    }

    @Transactional(readOnly = true)
    public List<AppealDto> getArbitratingAppeals() {
        List<GradeAppeal> appeals = gradeAppealRepository.findByStatus(AppealStatus.ARBITRATING);
        return appeals.stream().map(a -> {
            Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseId(a.getStudentId(), a.getCourseId()).orElse(null);
            return toDto(a, enrollment);
        }).collect(Collectors.toList());
    }

    @Transactional
    public AppealDto arbitrate(CustomUserDetails user, Long appealId, ArbitrateDto dto) {
        GradeAppeal appeal = gradeAppealRepository.findByIdForUpdate(appealId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "申诉不存在"));

        if (appeal.getStatus() != AppealStatus.ARBITRATING) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "只能仲裁ARBITRATING状态的申诉");
        }

        AppealStatus targetStatus;
        if ("APPROVED".equalsIgnoreCase(dto.getDecision())) {
            targetStatus = AppealStatus.APPROVED;
            if (dto.getFinalScore() == null) {
                throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "通过仲裁须填写最终分数");
            }
        } else if ("REJECTED".equalsIgnoreCase(dto.getDecision())) {
            targetStatus = AppealStatus.REJECTED;
        } else {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "decision必须为APPROVED或REJECTED");
        }

        AppealStatus from = appeal.getStatus();
        AppealStateMachine.validate(from, targetStatus);

        appeal.setStatus(targetStatus);
        appeal.setAdminComment(dto.getComment());
        if (targetStatus == AppealStatus.APPROVED) {
            appeal.setFinalScore(dto.getFinalScore());
        }
        appeal.setCurrentHandlerId(user.getUserId());
        appeal.setUpdatedAt(LocalDateTime.now());
        gradeAppealRepository.save(appeal);

        appealAuditService.writeAuditLog(appealId, from.name(), targetStatus.name(), user.getUserId(), "ROLE_SUPER_ADMIN", dto.getComment(), true, null);

        if (targetStatus == AppealStatus.APPROVED) {
            eventPublisher.publishEvent(new AppealApprovedEvent(appeal.getId(), appeal.getGradeId(), appeal.getFinalScore()));
        }

        Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseId(appeal.getStudentId(), appeal.getCourseId()).orElse(null);
        return toDto(appeal, enrollment);
    }

    @Transactional(readOnly = true)
    public AppealDto getDetail(CustomUserDetails user, Long appealId) {
        GradeAppeal appeal = gradeAppealRepository.findById(appealId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "申诉不存在"));
        Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseId(appeal.getStudentId(), appeal.getCourseId()).orElse(null);
        AppealDto dto = toDto(appeal, enrollment);

        boolean isStudent = appeal.getStudentId().equals(user.getUserId());
        boolean isSuperAdmin = user.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));
        boolean isTeacher = user.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_TEACHER"));

        if (!isStudent && !isSuperAdmin) {
            if (isTeacher) {
                Course course = courseRepository.findById(appeal.getCourseId()).orElse(null);
                if (course == null || !course.getTeacher().getId().equals(user.getUserId())) {
                    throw new BusinessException(ErrorCode.FORBIDDEN, "无权查看此申诉");
                }
            } else {
                throw new BusinessException(ErrorCode.FORBIDDEN, "无权查看此申诉");
            }
        }

        if (isStudent && !isSuperAdmin) {
            dto.setAdminComment(null);
        }
        if (isTeacher && !isSuperAdmin && !isStudent) {
            dto.setAdminComment(null);
        }
        return dto;
    }

    @Transactional(readOnly = true)
    public List<AuditLogDto> getAuditLogs(Long appealId) {
        return appealAuditLogRepository.findByAppealId(appealId).stream()
            .map(this::toAuditLogDto)
            .collect(Collectors.toList());
    }

    @Transactional
    public int closeRejectedOlderThan7Days() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(7);
        List<GradeAppeal> appeals = gradeAppealRepository.findByStatusAndUpdatedAtBefore(AppealStatus.REJECTED, cutoff);
        for (GradeAppeal appeal : appeals) {
            AppealStatus from = appeal.getStatus();
            appeal.setStatus(AppealStatus.CLOSED);
            appeal.setClosedAt(LocalDateTime.now());
            appeal.setUpdatedAt(LocalDateTime.now());
            gradeAppealRepository.save(appeal);

            appealAuditService.writeAuditLog(appeal.getId(), from.name(), AppealStatus.CLOSED.name(), null, "SYSTEM", "REJECTED满7天，定时任务自动关闭", true, null);
        }
        return appeals.size();
    }

    private AppealDto toDto(GradeAppeal a, Enrollment enrollment) {
        AppealDto dto = new AppealDto();
        dto.setId(a.getId());
        dto.setGradeId(a.getGradeId());
        dto.setStudentId(a.getStudentId());
        if (enrollment != null && enrollment.getStudent() != null) {
            dto.setStudentName(enrollment.getStudent().getRealName());
        }
        dto.setCourseId(a.getCourseId());
        if (enrollment != null && enrollment.getCourse() != null) {
            dto.setCourseName(enrollment.getCourse().getName());
        }
        dto.setOriginalScore(a.getOriginalScore());
        dto.setExpectedScore(a.getExpectedScore());
        dto.setFinalScore(a.getFinalScore());
        dto.setReason(a.getReason());
        dto.setTeacherComment(a.getTeacherComment());
        dto.setAdminComment(a.getAdminComment());
        dto.setStatus(a.getStatus().name());
        dto.setCurrentHandlerId(a.getCurrentHandlerId());
        dto.setCreatedAt(a.getCreatedAt());
        dto.setUpdatedAt(a.getUpdatedAt());
        dto.setClosedAt(a.getClosedAt());
        return dto;
    }

    private AuditLogDto toAuditLogDto(AppealAuditLog l) {
        AuditLogDto dto = new AuditLogDto();
        dto.setId(l.getId());
        dto.setAppealId(l.getAppealId());
        dto.setFromStatus(l.getFromStatus());
        dto.setToStatus(l.getToStatus());
        dto.setOperatorId(l.getOperatorId());
        dto.setOperatorRole(l.getOperatorRole());
        dto.setComment(l.getComment());
        dto.setSuccess(l.getSuccess());
        dto.setErrorMsg(l.getErrorMsg());
        dto.setCreatedAt(l.getCreatedAt());
        return dto;
    }
}
