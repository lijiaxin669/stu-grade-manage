package com.example.grade.appeal.repository;

import com.example.grade.appeal.domain.AppealAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AppealAuditLogRepository extends JpaRepository<AppealAuditLog, Long> {

    @Query("SELECT l FROM AppealAuditLog l WHERE l.appealId = :appealId ORDER BY l.createdAt ASC")
    List<AppealAuditLog> findByAppealId(@Param("appealId") Long appealId);
}
