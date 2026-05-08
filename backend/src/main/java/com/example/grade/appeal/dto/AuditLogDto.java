package com.example.grade.appeal.dto;

import com.example.grade.appeal.domain.AppealStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogDto {
    private Long id;
    private Long appealId;
    private AppealStatus fromStatus;
    private AppealStatus toStatus;
    private Long operatorId;
    private String operatorName;
    private String operatorRole;
    private String comment;
    private boolean success;
    private String errorMsg;
    private LocalDateTime createdAt;
}
