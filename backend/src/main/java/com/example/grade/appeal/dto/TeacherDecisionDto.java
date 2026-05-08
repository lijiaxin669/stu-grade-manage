package com.example.grade.appeal.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class TeacherDecisionDto {
    @NotBlank
    @Pattern(regexp = "^(APPROVED|REJECTED)$")
    private String decision;

    @NotBlank
    @Size(max = 500)
    private String comment;

    @Min(0)
    @Max(100)
    private Double finalScore;
}
