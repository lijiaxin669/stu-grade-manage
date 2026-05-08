package com.example.grade.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserUpdateDto {
    @NotBlank(message = "Real name cannot be empty")
    private String realName;

    // Optional: allow switching between TEACHER/STUDENT
    private String roleCode; // ROLE_TEACHER / ROLE_STUDENT
}

