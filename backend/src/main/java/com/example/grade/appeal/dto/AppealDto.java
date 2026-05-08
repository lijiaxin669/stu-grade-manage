package com.example.grade.appeal.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AppealDto {
    private Long id;
    private Long gradeId;
    private Long studentId;
    private String studentName;
    private Long courseId;
    private String courseName;
    private BigDecimal originalScore;
    private BigDecimal expectedScore;
    private BigDecimal finalScore;
    private String reason;
    private String teacherComment;
    private String adminComment;
    private String status;
    private Long currentHandlerId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime closedAt;
}
