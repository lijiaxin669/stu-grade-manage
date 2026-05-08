package com.example.grade.appeal.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class AppealCreateDto {
    @NotNull
    private Long gradeId;

    @NotNull
    @Min(0)
    @Max(100)
    private Double expectedScore;

    @NotBlank
    @Size(min = 10, max = 500)
    private String reason;
}
