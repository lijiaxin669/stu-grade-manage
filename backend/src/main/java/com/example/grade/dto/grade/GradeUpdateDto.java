package com.example.grade.dto.grade;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GradeUpdateDto {
    @NotNull
    @Min(0)
    @Max(100)
    private Double score;

    private String type;
}

