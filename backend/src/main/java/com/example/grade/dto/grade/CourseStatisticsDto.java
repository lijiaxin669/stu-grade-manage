package com.example.grade.dto.grade;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseStatisticsDto {
    private Long courseId;
    private String courseName;
    private Double averageScore;
    private Double passRate;     // 0.0 - 1.0
    private Long studentCount;   // Total students (with grades)
    private Long passedCount;
    private BigDecimal maxScore;
    private BigDecimal minScore;
}
