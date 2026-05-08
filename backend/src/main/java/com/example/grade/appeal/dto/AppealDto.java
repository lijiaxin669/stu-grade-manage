package com.example.grade.appeal.dto;

import com.example.grade.appeal.domain.AppealStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppealDto {
    private Long id;
    private Long gradeId;
    private Long studentId;
    private String studentName;
    private String studentUsername;
    private Long courseId;
    private String courseName;
    private BigDecimal originalScore;
    private BigDecimal expectedScore;
    private BigDecimal finalScore;
    private String reason;
    private String teacherComment;
    private String adminComment;
    private AppealStatus status;
    private Long currentHandlerId;
    private String currentHandlerName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime closedAt;
}
