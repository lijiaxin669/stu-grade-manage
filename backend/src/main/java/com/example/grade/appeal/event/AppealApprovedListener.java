package com.example.grade.appeal.event;

import com.example.grade.appeal.domain.Appeal;
import com.example.grade.appeal.domain.AppealAuditLog;
import com.example.grade.appeal.domain.AppealStatus;
import com.example.grade.appeal.repository.AppealAuditLogRepository;
import com.example.grade.appeal.repository.AppealRepository;
import com.example.grade.appeal.util.AppealStateMachine;
import com.example.grade.domain.Grade;
import com.example.grade.repository.GradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class AppealApprovedListener {

    private final GradeRepository gradeRepository;
    private final AppealRepository appealRepository;
    private final AppealAuditLogRepository auditLogRepository;

    @EventListener
    @Transactional
    public void onAppealApproved(AppealApprovedEvent event) {
        Grade grade = gradeRepository.findById(event.getGradeId()).orElse(null);
        Appeal appeal = appealRepository.findById(event.getAppealId()).orElse(null);

        if (grade == null || appeal == null) {
            return;
        }

        AppealStatus fromStatus = appeal.getStatus();
        AppealStateMachine.validateTransition(fromStatus, AppealStatus.CLOSED);

        grade.setScore(event.getFinalScore());
        grade.setUpdatedAt(LocalDateTime.now());
        gradeRepository.save(grade);

        appeal.setStatus(AppealStatus.CLOSED);
        appeal.setClosedAt(LocalDateTime.now());
        appealRepository.save(appeal);

        saveAuditLog(event.getAppealId(), fromStatus, AppealStatus.CLOSED,
                event.getOperatorId(), event.getOperatorRole(),
                "申诉批准，成绩已调整为 " + event.getFinalScore() + "，申诉关闭",
                true, null);
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
