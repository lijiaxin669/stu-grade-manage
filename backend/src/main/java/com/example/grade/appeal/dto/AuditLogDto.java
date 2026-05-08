package com.example.grade.appeal.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AuditLogDto {
    private Long id;
    private Long appealId;
    private String fromStatus;
    private String toStatus;
    private Long operatorId;
    private String operatorRole;
    private String comment;
    private Boolean success;
    private String errorMsg;
    private LocalDateTime createdAt;
}
