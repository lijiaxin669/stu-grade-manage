package com.example.grade.appeal.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ArbitrateRequestDto {
    @NotNull(message = "reason不能为空")
    private String reason;
}
