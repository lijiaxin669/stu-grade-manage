package com.example.grade.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterDto {
    @NotBlank(message = "Username cannot be empty")
    @Size(min = 4, max = 20, message = "Username must be between 4 and 20 characters")
    private String username;

    @NotBlank(message = "Real name cannot be empty")
    private String realName;

    @NotBlank(message = "Password cannot be empty")
    @Size(min = 6, max = 50, message = "Password must be between 6 and 50 characters")
    private String password;

    /**
     * Only ROLE_STUDENT / ROLE_TEACHER can self-register.
     */
    @NotBlank(message = "Role is required")
    private String roleCode;
}
