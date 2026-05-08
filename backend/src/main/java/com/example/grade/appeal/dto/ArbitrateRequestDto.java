package com.example.grade.appeal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ArbitrateRequestDto {
    @NotBlank
    @Size(min = 10, max = 500)
    private String reason;
}
