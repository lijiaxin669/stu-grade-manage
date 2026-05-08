package com.example.grade.domain;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "sys_permissions")
public class Permission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code; // e.g. MENU_ADMIN_USERS, API_USER_LIST

    @Column(nullable = false)
    private String name; // human readable

    @Column(nullable = false)
    private String type; // MENU | API
}

