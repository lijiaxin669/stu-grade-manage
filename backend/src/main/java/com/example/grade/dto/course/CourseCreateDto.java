package com.example.grade.dto.course;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CourseCreateDto {
    @NotBlank(message = "课程名不能为空")
    private String name;

    @NotNull(message = "学分不能为空")
    @Min(value = 1, message = "学分至少为1")
    private Integer credits;

    @NotBlank(message = "学期不能为空")
    private String semester;

    private Long teacherId; // 管理员可指定，教师默认自己
    private String description;
}
