package com.example.grade.dto.user;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserDto {
    private Long id;
    private String username;
    private String realName;
    private String roleCode;
    private Integer status;
    private LocalDateTime createdAt;
}
