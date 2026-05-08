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

    @Column(name = "from_status", length = 20)
    private String fromStatus;

    @Column(name = "to_status", length = 20)
    private String toStatus;

    @Column(name = "operator_id")
    private Long operatorId;

    @Column(name = "operator_role", length = 30)
    private String operatorRole;

    @Column(length = 500)
    private String comment;

    @Column(nullable = false)
    private Boolean success;

    @Column(name = "error_msg", length = 500)
    private String errorMsg;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
