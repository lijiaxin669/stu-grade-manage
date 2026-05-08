package com.example.grade.appeal.repository;

import com.example.grade.appeal.domain.AppealAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppealAuditLogRepository extends JpaRepository<AppealAuditLog, Long> {
    List<AppealAuditLog> findByAppealIdOrderByCreatedAtAsc(Long appealId);
}
