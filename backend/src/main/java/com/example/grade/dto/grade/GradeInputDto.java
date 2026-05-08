package com.example.grade.dto.grade;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class GradeInputDto {
    @NotNull(message = "学生ID不能为空")
    private Long studentId;
    
    @NotNull(message = "课程ID不能为空")
    private Long courseId;
    
    @NotNull(message = "分数不能为空")
    @DecimalMin(value = "0.0", message = "分数不能低于0")
    @DecimalMax(value = "100.0", message = "分数不能超过100")
    private BigDecimal score;
    
    private String type = "FINAL";
}
