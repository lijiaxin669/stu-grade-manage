package com.example.grade.controller;

import com.example.grade.common.Result;
import com.example.grade.domain.Permission;
import com.example.grade.repository.PermissionRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionRepository permissionRepository;

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<List<Permission>> listAll() {
        return Result.success(permissionRepository.findAll());
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Void> create(@Valid @RequestBody PermissionSaveDto dto) {
        Permission p = new Permission();
        p.setCode(dto.getCode());
        p.setName(dto.getName());
        p.setType(dto.getType());
        permissionRepository.save(p);
        return Result.success();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody PermissionSaveDto dto) {
        Permission p = permissionRepository.findById(id).orElseThrow();
        p.setCode(dto.getCode());
        p.setName(dto.getName());
        p.setType(dto.getType());
        permissionRepository.save(p);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        permissionRepository.deleteById(id);
        return Result.success();
    }

    @Data
    public static class PermissionSaveDto {
        @NotBlank
        private String code;
        @NotBlank
        private String name;
        @NotBlank
        private String type; // MENU|API
    }
}

