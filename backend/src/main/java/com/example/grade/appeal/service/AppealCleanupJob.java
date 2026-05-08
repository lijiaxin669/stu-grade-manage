package com.example.grade.appeal.service;

import com.example.grade.appeal.domain.Appeal;
import com.example.grade.appeal.domain.AppealAuditLog;
import com.example.grade.appeal.domain.AppealStatus;
import com.example.grade.appeal.repository.AppealAuditLogRepository;
import com.example.grade.appeal.repository.AppealRepository;
import com.example.grade.appeal.util.AppealStateMachine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppealCleanupJob {

    private final AppealRepository appealRepository;
    private final AppealAuditLogRepository auditLogRepository;

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void closeExpiredRejectedAppeals() {
        log.info("Starting appeal cleanup job...");

        try {
            LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
            List<Appeal> rejectedAppeals = appealRepository.findByStatusAndUpdatedAtBefore(
                    AppealStatus.REJECTED, sevenDaysAgo);

            if (rejectedAppeals == null || rejectedAppeals.isEmpty()) {
                log.info("No expired rejected appeals to close.");
                return;
            }

            for (Appeal appeal : rejectedAppeals) {
                try {
                    AppealStatus fromStatus = appeal.getStatus();
                    AppealStateMachine.validateTransition(fromStatus, AppealStatus.CLOSED);

                    appeal.setStatus(AppealStatus.CLOSED);
                    appeal.setClosedAt(LocalDateTime.now());
                    appealRepository.save(appeal);

                    saveAuditLog(appeal.getId(), fromStatus, AppealStatus.CLOSED,
                            "系统自动关闭：驳回超过7天", true, null);

                    log.info("Auto-closed appeal id={}, updatedAt={}", appeal.getId(), appeal.getUpdatedAt());
                } catch (Exception e) {
                    log.error("Failed to close appeal id={}: {}", appeal.getId(), e.getMessage());
                    try {
                        saveAuditLog(appeal.getId(), appeal.getStatus(), null,
                                "系统自动关闭失败", false, e.getMessage());
                    } catch (Exception ex) {
                        log.error("Failed to save audit log for appeal id={}: {}", appeal.getId(), ex.getMessage());
                    }
                }
            }

            log.info("Appeal cleanup job completed. Closed {} appeals.", rejectedAppeals.size());
        } catch (Exception e) {
            log.error("Appeal cleanup job failed with exception", e);
        }
    }

    private void saveAuditLog(Long appealId, AppealStatus from, AppealStatus to,
                               String comment, boolean success, String errorMsg) {
        if (appealId == null) {
            return;
        }
        AppealAuditLog auditLog = new AppealAuditLog();
        auditLog.setAppealId(appealId);
        auditLog.setFromStatus(from);
        auditLog.setToStatus(to);
        auditLog.setOperatorId(0L);
        auditLog.setOperatorRole("SYSTEM");
        auditLog.setComment(comment);
        auditLog.setSuccess(success);
        auditLog.setErrorMsg(errorMsg);
        auditLogRepository.save(auditLog);
    }
}
