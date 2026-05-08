package com.example.grade.appeal.service;

import com.example.grade.appeal.domain.Appeal;
import com.example.grade.appeal.domain.AppealAuditLog;
import com.example.grade.appeal.domain.AppealStatus;
import com.example.grade.appeal.dto.*;
import com.example.grade.appeal.event.AppealApprovedEvent;
import com.example.grade.appeal.repository.AppealAuditLogRepository;
import com.example.grade.appeal.repository.AppealRepository;
import com.example.grade.appeal.util.AppealStateMachine;
import com.example.grade.common.ErrorCode;
import com.example.grade.domain.*;
import com.example.grade.exception.BusinessException;
import com.example.grade.repository.*;
import com.example.grade.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppealService {

    private final AppealRepository appealRepository;
    private final AppealAuditLogRepository auditLogRepository;
    private final GradeRepository gradeRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Long createAppeal(CustomUserDetails student, AppealCreateDto dto) {
        boolean isStudent = student.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_STUDENT"));
        if (!isStudent) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        Grade grade = gradeRepository.findById(dto.getGradeId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "成绩不存在"));

        if (grade.getEnrollment() == null || 
            grade.getEnrollment().getStudent() == null ||
            !grade.getEnrollment().getStudent().getId().equals(student.getUserId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        LocalDateTime gradeUpdatedAt = grade.getUpdatedAt();
        if (gradeUpdatedAt == null || gradeUpdatedAt.isBefore(sevenDaysAgo)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "成绩录入超过7天，无法申诉");
        }

        if (appealRepository.existsByGradeIdAndStatusNot(dto.getGradeId(), AppealStatus.CLOSED)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "该成绩已有进行中的申诉");
        }

        BigDecimal originalScore = grade.getScore();
        BigDecimal expectedScore = BigDecimal.valueOf(dto.getExpectedScore());
        if (originalScore.subtract(expectedScore).abs().compareTo(BigDecimal.valueOf(2)) < 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "期望分数与原分数差异必须≥2分");
        }

        if (grade.getEnrollment().getCourse() == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "课程信息不存在");
        }

        Appeal appeal = new Appeal();
        appeal.setGradeId(dto.getGradeId());
        appeal.setStudentId(student.getUserId());
        appeal.setCourseId(grade.getEnrollment().getCourse().getId());
        appeal.setOriginalScore(originalScore);
        appeal.setExpectedScore(expectedScore);
        appeal.setReason(dto.getReason());
        appeal.setStatus(AppealStatus.PENDING);
        appeal = appealRepository.save(appeal);

        saveAuditLog(appeal.getId(), null, AppealStatus.PENDING, 
                student.getUserId(), "ROLE_STUDENT", "创建申诉: " + dto.getReason(), 
                true, null);

        return appeal.getId();
    }

    public List<AppealDto> getStudentAppeals(CustomUserDetails student) {
        List<Appeal> appeals = appealRepository.findByStudentIdOrderByCreatedAtDesc(student.getUserId());
        return toAppealDtos(appeals);
    }

    public List<AppealDto> getTeacherPendingAppeals(CustomUserDetails teacher) {
        boolean isTeacher = teacher.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_TEACHER"));
        if (!isTeacher) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        List<Long> courseIds = courseRepository.findByTeacherId(teacher.getUserId())
                .stream()
                .map(Course::getId)
                .collect(Collectors.toList());

        if (courseIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<AppealStatus> statuses = Arrays.asList(AppealStatus.PENDING, AppealStatus.UNDER_REVIEW);
        List<Appeal> appeals = appealRepository.findByCourseIdInAndStatusIn(courseIds, statuses);
        return toAppealDtos(appeals);
    }

    @Transactional
    public void claimAppeal(CustomUserDetails teacher, Long appealId) {
        boolean isTeacher = teacher.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_TEACHER"));
        if (!isTeacher) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        Appeal appeal = appealRepository.findByIdWithLock(appealId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "申诉不存在"));

        Course course = courseRepository.findById(appeal.getCourseId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "课程不存在"));

        if (!course.getTeacher().getId().equals(teacher.getUserId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        if (appeal.getStatus() == AppealStatus.UNDER_REVIEW) {
            if (appeal.getCurrentHandlerId() != null && 
                !appeal.getCurrentHandlerId().equals(teacher.getUserId())) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "该申诉已被认领");
            }
            return;
        }

        if (appeal.getStatus() != AppealStatus.PENDING) {
            saveAuditLog(appealId, appeal.getStatus(), null,
                    teacher.getUserId(), "ROLE_TEACHER", "认领失败",
                    false, "当前状态不是PENDING");
            throw new BusinessException(ErrorCode.PARAM_ERROR, "该申诉已被认领或已处理");
        }

        AppealStateMachine.validateTransition(appeal.getStatus(), AppealStatus.UNDER_REVIEW);

        appeal.setStatus(AppealStatus.UNDER_REVIEW);
        appeal.setCurrentHandlerId(teacher.getUserId());
        appealRepository.save(appeal);

        saveAuditLog(appealId, AppealStatus.PENDING, AppealStatus.UNDER_REVIEW,
                teacher.getUserId(), "ROLE_TEACHER", "教师认领申诉",
                true, null);
    }

    @Transactional
    public void teacherDecision(CustomUserDetails teacher, Long appealId, TeacherDecisionDto dto) {
        boolean isTeacher = teacher.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_TEACHER"));
        if (!isTeacher) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        Appeal appeal = appealRepository.findById(appealId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "申诉不存在"));

        if (appeal.getStatus() != AppealStatus.UNDER_REVIEW) {
            saveAuditLog(appealId, appeal.getStatus(), null,
                    teacher.getUserId(), "ROLE_TEACHER", "裁定失败: " + dto.getComment(),
                    false, "当前状态不是UNDER_REVIEW");
            throw new BusinessException(ErrorCode.PARAM_ERROR, "申诉状态不正确");
        }

        if (appeal.getCurrentHandlerId() == null || 
            !appeal.getCurrentHandlerId().equals(teacher.getUserId())) {
            saveAuditLog(appealId, appeal.getStatus(), null,
                    teacher.getUserId(), "ROLE_TEACHER", "裁定失败: " + dto.getComment(),
                    false, "请先认领该申诉");
            throw new BusinessException(ErrorCode.PARAM_ERROR, "请先认领该申诉");
        }

        Course course = courseRepository.findById(appeal.getCourseId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "课程不存在"));
        if (!course.getTeacher().getId().equals(teacher.getUserId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        AppealStatus targetStatus = AppealStatus.valueOf(dto.getDecision());
        AppealStateMachine.validateTransition(appeal.getStatus(), targetStatus);

        appeal.setTeacherComment(dto.getComment());
        if (targetStatus == AppealStatus.APPROVED) {
            if (dto.getFinalScore() == null) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "批准时必须提供最终分数");
            }
            appeal.setFinalScore(BigDecimal.valueOf(dto.getFinalScore()));
        }

        appeal.setStatus(targetStatus);
        appealRepository.save(appeal);

        saveAuditLog(appealId, AppealStatus.UNDER_REVIEW, targetStatus,
                teacher.getUserId(), "ROLE_TEACHER", 
                (targetStatus == AppealStatus.APPROVED ? "批准" : "驳回") + ": " + dto.getComment(),
                true, null);

        if (targetStatus == AppealStatus.APPROVED) {
            eventPublisher.publishEvent(new AppealApprovedEvent(
                    this, appealId, appeal.getGradeId(), appeal.getFinalScore(),
                    teacher.getUserId(), "ROLE_TEACHER"));
        }
    }

    @Transactional
    public void requestArbitration(CustomUserDetails student, Long appealId, ArbitrateRequestDto dto) {
        boolean isStudent = student.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_STUDENT"));
        if (!isStudent) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        Appeal appeal = appealRepository.findById(appealId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "申诉不存在"));

        if (!appeal.getStudentId().equals(student.getUserId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        if (appeal.getStatus() != AppealStatus.REJECTED) {
            saveAuditLog(appealId, appeal.getStatus(), null,
                    student.getUserId(), "ROLE_STUDENT", "仲裁请求失败: " + dto.getReason(),
                    false, "当前状态不是REJECTED");
            throw new BusinessException(ErrorCode.PARAM_ERROR, "只有被驳回的申诉才能发起仲裁");
        }

        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);
        if (appeal.getUpdatedAt().isBefore(threeDaysAgo)) {
            saveAuditLog(appealId, appeal.getStatus(), null,
                    student.getUserId(), "ROLE_STUDENT", "仲裁请求失败: " + dto.getReason(),
                    false, "驳回已超过3天");
            throw new BusinessException(ErrorCode.PARAM_ERROR, "驳回已超过3天，无法发起仲裁");
        }

        AppealStateMachine.validateTransition(appeal.getStatus(), AppealStatus.ARBITRATING);

        appeal.setStatus(AppealStatus.ARBITRATING);
        appealRepository.save(appeal);

        saveAuditLog(appealId, AppealStatus.REJECTED, AppealStatus.ARBITRATING,
                student.getUserId(), "ROLE_STUDENT", "发起仲裁请求: " + dto.getReason(),
                true, null);
    }

    public List<AppealDto> getArbitratingAppeals(CustomUserDetails admin) {
        boolean isAdmin = admin.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"));
        if (!isAdmin) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        List<Appeal> appeals = appealRepository.findByStatusOrderByCreatedAtAsc(AppealStatus.ARBITRATING);
        return toAppealDtos(appeals);
    }

    @Transactional
    public void adminArbitrate(CustomUserDetails admin, Long appealId, AdminArbitrateDto dto) {
        boolean isAdmin = admin.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"));
        if (!isAdmin) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        Appeal appeal = appealRepository.findById(appealId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "申诉不存在"));

        if (appeal.getStatus() != AppealStatus.ARBITRATING) {
            saveAuditLog(appealId, appeal.getStatus(), null,
                    admin.getUserId(), "ROLE_SUPER_ADMIN", "仲裁失败: " + dto.getComment(),
                    false, "当前状态不是ARBITRATING");
            throw new BusinessException(ErrorCode.PARAM_ERROR, "申诉状态不正确");
        }

        AppealStatus targetStatus = AppealStatus.valueOf(dto.getDecision());
        AppealStateMachine.validateTransition(appeal.getStatus(), targetStatus);

        appeal.setAdminComment(dto.getComment());
        if (targetStatus == AppealStatus.APPROVED) {
            if (dto.getFinalScore() == null) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "批准时必须提供最终分数");
            }
            appeal.setFinalScore(BigDecimal.valueOf(dto.getFinalScore()));
        }

        appeal.setStatus(targetStatus);
        appealRepository.save(appeal);

        saveAuditLog(appealId, AppealStatus.ARBITRATING, targetStatus,
                admin.getUserId(), "ROLE_SUPER_ADMIN",
                (targetStatus == AppealStatus.APPROVED ? "仲裁批准" : "仲裁驳回") + ": " + dto.getComment(),
                true, null);

        if (targetStatus == AppealStatus.APPROVED) {
            eventPublisher.publishEvent(new AppealApprovedEvent(
                    this, appealId, appeal.getGradeId(), appeal.getFinalScore(),
                    admin.getUserId(), "ROLE_SUPER_ADMIN"));
        }
    }

    public AppealDto getAppealDetail(CustomUserDetails user, Long appealId) {
        Appeal appeal = appealRepository.findById(appealId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "申诉不存在"));

        boolean isAdmin = user.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"));
        boolean isTeacher = user.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_TEACHER"));
        boolean isStudent = user.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_STUDENT"));

        if (isStudent && !appeal.getStudentId().equals(user.getUserId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        if (isTeacher) {
            Course course = courseRepository.findById(appeal.getCourseId()).orElse(null);
            if (course == null || !course.getTeacher().getId().equals(user.getUserId())) {
                throw new BusinessException(ErrorCode.FORBIDDEN);
            }
        }

        return toAppealDto(appeal);
    }

    public List<AuditLogDto> getAuditLogs(CustomUserDetails user, Long appealId) {
        Appeal appeal = appealRepository.findById(appealId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "申诉不存在"));

        boolean isAdmin = user.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"));
        boolean isTeacher = user.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_TEACHER"));
        boolean isStudent = user.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_STUDENT"));

        if (isStudent && !appeal.getStudentId().equals(user.getUserId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        if (isTeacher) {
            Course course = courseRepository.findById(appeal.getCourseId()).orElse(null);
            if (course == null || !course.getTeacher().getId().equals(user.getUserId())) {
                throw new BusinessException(ErrorCode.FORBIDDEN);
            }
        }

        List<AppealAuditLog> logs = auditLogRepository.findByAppealIdOrderByCreatedAtAsc(appealId);
        return toAuditLogDtos(logs);
    }

    private List<AppealDto> toAppealDtos(List<Appeal> appeals) {
        if (appeals == null || appeals.isEmpty()) return Collections.emptyList();

        Set<Long> userIds = new HashSet<>();
        Set<Long> courseIds = new HashSet<>();
        for (Appeal a : appeals) {
            if (a.getStudentId() != null) {
                userIds.add(a.getStudentId());
            }
            if (a.getCurrentHandlerId() != null) {
                userIds.add(a.getCurrentHandlerId());
            }
            if (a.getCourseId() != null) {
                courseIds.add(a.getCourseId());
            }
        }

        Map<Long, User> userMap = userIds.isEmpty() ? Collections.emptyMap() : 
            userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u, (existing, replacement) -> existing));
        Map<Long, Course> courseMap = courseIds.isEmpty() ? Collections.emptyMap() : 
            courseRepository.findAllById(courseIds).stream()
                .collect(Collectors.toMap(Course::getId, c -> c, (existing, replacement) -> existing));

        return appeals.stream().map(a -> {
            User student = a.getStudentId() != null ? userMap.get(a.getStudentId()) : null;
            Course course = a.getCourseId() != null ? courseMap.get(a.getCourseId()) : null;
            User handler = a.getCurrentHandlerId() != null ? userMap.get(a.getCurrentHandlerId()) : null;

            return new AppealDto(
                    a.getId(),
                    a.getGradeId(),
                    a.getStudentId(),
                    student != null ? student.getRealName() : null,
                    student != null ? student.getUsername() : null,
                    a.getCourseId(),
                    course != null ? course.getName() : null,
                    a.getOriginalScore(),
                    a.getExpectedScore(),
                    a.getFinalScore(),
                    a.getReason(),
                    a.getTeacherComment(),
                    a.getAdminComment(),
                    a.getStatus(),
                    a.getCurrentHandlerId(),
                    handler != null ? handler.getRealName() : null,
                    a.getCreatedAt(),
                    a.getUpdatedAt(),
                    a.getClosedAt()
            );
        }).collect(Collectors.toList());
    }

    private AppealDto toAppealDto(Appeal appeal) {
        return toAppealDtos(Collections.singletonList(appeal)).get(0);
    }

    private List<AuditLogDto> toAuditLogDtos(List<AppealAuditLog> logs) {
        if (logs == null || logs.isEmpty()) return Collections.emptyList();

        Set<Long> userIds = logs.stream()
                .map(AppealAuditLog::getOperatorId)
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toSet());

        Map<Long, User> userMap = userIds.isEmpty() ? Collections.emptyMap() : 
            userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u, (existing, replacement) -> existing));

        return logs.stream().map(log -> {
            User operator = (log.getOperatorId() != null && log.getOperatorId() > 0) 
                ? userMap.get(log.getOperatorId()) : null;
            return new AuditLogDto(
                    log.getId(),
                    log.getAppealId(),
                    log.getFromStatus(),
                    log.getToStatus(),
                    log.getOperatorId(),
                    operator != null ? operator.getRealName() : 
                        ("SYSTEM".equals(log.getOperatorRole()) ? "系统" : null),
                    log.getOperatorRole(),
                    log.getComment(),
                    log.isSuccess(),
                    log.getErrorMsg(),
                    log.getCreatedAt()
            );
        }).collect(Collectors.toList());
    }

    private void saveAuditLog(Long appealId, AppealStatus from, AppealStatus to,
                               Long operatorId, String operatorRole, String comment,
                               boolean success, String errorMsg) {
        AppealAuditLog log = new AppealAuditLog();
        log.setAppealId(appealId);
        log.setFromStatus(from);
        log.setToStatus(to);
        log.setOperatorId(operatorId);
        log.setOperatorRole(operatorRole);
        log.setComment(comment);
        log.setSuccess(success);
        log.setErrorMsg(errorMsg);
        auditLogRepository.save(log);
    }
}
