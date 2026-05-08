package com.example.grade.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserCreateDto {
    @NotBlank(message = "Username cannot be empty")
    @Size(min = 4, max = 20, message = "Username must be between 4 and 20 characters")
    private String username;

    @NotBlank(message = "Real name cannot be empty")
    private String realName;

    @NotNull(message = "Role is required")
    private String roleCode; // "ROLE_TEACHER", "ROLE_STUDENT"
}
