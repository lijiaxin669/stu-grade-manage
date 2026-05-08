package com.example.grade.dto.user;

import lombok.Data;

@Data
public class UserQueryDto {
    private String keyword; // Search by username or realName
    private String roleCode;
    private int pageNum = 1;
    private int pageSize = 10;
}
