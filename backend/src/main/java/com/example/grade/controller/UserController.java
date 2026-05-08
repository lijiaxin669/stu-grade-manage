package com.example.grade.controller;

import com.example.grade.common.Result;
import com.example.grade.common.PageResult;
import com.example.grade.dto.user.UserCreateDto;
import com.example.grade.dto.user.UserDto;
import com.example.grade.dto.user.UserQueryDto;
import com.example.grade.dto.user.UserUpdateDto;
import com.example.grade.security.CustomUserDetails;
import com.example.grade.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAuthority('API_USER_LIST')")
    public Result<PageResult<UserDto>> list(UserQueryDto query) {
        return Result.success(userService.listUsers(query));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('API_USER_CREATE')")
    public Result<Void> create(@AuthenticationPrincipal CustomUserDetails currentUser,
                               @Valid @RequestBody UserCreateDto dto) {
        userService.createUser(currentUser, dto);
        return Result.success();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('API_USER_UPDATE')")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody UserUpdateDto dto) {
        userService.updateUser(id, dto);
        return Result.success();
    }

    @PutMapping("/{id}/password/reset")
    @PreAuthorize("hasAuthority('API_USER_PASSWORD_RESET')")
    public Result<Void> resetPassword(@PathVariable Long id) {
        userService.resetPassword(id);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('API_USER_STATUS')")
    public Result<Void> toggleStatus(@PathVariable Long id, @RequestParam Integer status) {
        userService.toggleStatus(id, status);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('API_USER_DELETE')")
    public Result<Void> delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success();
    }

    @PutMapping("/{id}/roles")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Void> setRoles(@PathVariable Long id, @RequestBody List<String> roleCodes) {
        userService.setRoles(id, roleCodes);
        return Result.success();
    }
}
