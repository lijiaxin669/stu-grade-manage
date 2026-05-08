package com.example.grade.controller;

import com.example.grade.common.Result;
import com.example.grade.domain.Permission;
import com.example.grade.domain.Role;
import com.example.grade.repository.PermissionRepository;
import com.example.grade.repository.RoleRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<List<Role>> listAll() {
        return Result.success(roleRepository.findAll());
    }

    @GetMapping("/{id}/permissions")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Set<String>> getPermissions(@PathVariable Long id) {
        Role role = roleRepository.findById(id).orElseThrow();
        return Result.success(role.getPermissions().stream().map(Permission::getCode).collect(Collectors.toSet()));
    }

    @PutMapping("/{id}/permissions")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Void> setPermissions(@PathVariable Long id, @Valid @RequestBody RolePermissionsDto dto) {
        Role role = roleRepository.findById(id).orElseThrow();
        Set<Permission> perms = dto.getPermissionCodes().stream()
            .map(code -> permissionRepository.findByCode(code).orElseThrow())
            .collect(Collectors.toSet());
        role.setPermissions(perms);
        roleRepository.save(role);
        return Result.success();
    }

    @Data
    public static class RolePermissionsDto {
        @NotEmpty
        private List<String> permissionCodes;
    }
}

