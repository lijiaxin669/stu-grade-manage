package com.example.grade.dto.course;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CourseDto {
    private Long id;
    private String name;
    private String description;
    private Integer credits;
    private String semester;
    private Long teacherId;
    private String teacherName;
    private LocalDateTime createdAt;
}
