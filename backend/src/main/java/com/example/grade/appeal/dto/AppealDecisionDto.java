package com.example.grade.appeal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AppealDecisionDto {
    @NotBlank(message = "decision不能为空")
    private String decision;

    @NotNull(message = "comment不能为空")
    private String comment;

    private BigDecimal finalScore;
}
