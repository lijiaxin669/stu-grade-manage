package com.example.grade.appeal.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "appeal_audit_log")
public class AppealAuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "appeal_id", nullable = false)
    private Long appealId;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", length = 20)
    private AppealStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", length = 20)
    private AppealStatus toStatus;

    @Column(name = "operator_id", nullable = false)
    private Long operatorId;

    @Column(name = "operator_role", nullable = false, length = 30)
    private String operatorRole;

    @Column(name = "comment", length = 500)
    private String comment;

    @Column(nullable = false)
    private boolean success = true;

    @Column(name = "error_msg", length = 500)
    private String errorMsg;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
