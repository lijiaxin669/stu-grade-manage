package com.example.grade.appeal.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AppealCreateDto {
    @NotNull(message = "gradeId不能为空")
    private Long gradeId;

    @NotNull(message = "expectedScore不能为空")
    @DecimalMin(value = "0", message = "expectedScore不能小于0")
    @DecimalMax(value = "100", message = "expectedScore不能大于100")
    private BigDecimal expectedScore;

    @NotNull(message = "reason不能为空")
    @Size(min = 10, max = 500, message = "reason长度需在10-500之间")
    private String reason;
}
