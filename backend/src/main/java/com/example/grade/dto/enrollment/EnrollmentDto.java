package com.example.grade.dto.enrollment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EnrollmentDto {
    private Long enrollmentId;
    private Long studentId;
    private String studentName;
    private String studentUsername;
    private Long courseId;
}
