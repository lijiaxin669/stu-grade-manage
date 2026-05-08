package com.example.grade.dto.course;

import lombok.Data;

@Data
public class CourseUpdateDto {
    private String name;
    private String description;
    private Integer credits;
    private String semester;
    private Long teacherId; // 仅管理员可变更教师
}
