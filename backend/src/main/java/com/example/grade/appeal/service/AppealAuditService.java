package com.example.grade.appeal.service;

import com.example.grade.appeal.domain.AppealAuditLog;
import com.example.grade.appeal.repository.AppealAuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AppealAuditService {

    private final AppealAuditLogRepository appealAuditLogRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void writeAuditLog(Long appealId, String fromStatus, String toStatus,
                              Long operatorId, String operatorRole, String comment,
                              boolean success, String errorMsg) {
        AppealAuditLog auditLog = new AppealAuditLog();
        auditLog.setAppealId(appealId);
        auditLog.setFromStatus(fromStatus);
        auditLog.setToStatus(toStatus);
        auditLog.setOperatorId(operatorId);
        auditLog.setOperatorRole(operatorRole);
        auditLog.setComment(comment);
        auditLog.setSuccess(success);
        auditLog.setErrorMsg(errorMsg);
        appealAuditLogRepository.save(auditLog);
    }
}
