package com.example.grade.dto.grade;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GradeDto {
    private Long id;
    private Long studentId;
    private String studentName;
    private String studentUsername; // was studentNo
    private String courseName;
    private BigDecimal score;
    private String semester;
    private java.time.LocalDateTime gradedAt;
}
