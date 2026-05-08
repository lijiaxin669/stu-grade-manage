package com.example.grade.controller;

import com.example.grade.common.ErrorCode;
import com.example.grade.common.Result;
import com.example.grade.domain.Role;
import com.example.grade.domain.User;
import com.example.grade.dto.auth.ChangePasswordDto;
import com.example.grade.dto.auth.RegisterDto;
import com.example.grade.exception.BusinessException;
import com.example.grade.repository.RoleRepository;
import com.example.grade.repository.UserRepository;
import com.example.grade.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/me")
    public Result<Object> me(@AuthenticationPrincipal CustomUserDetails user) {
        return Result.success(user);
    }

    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterDto dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new BusinessException(ErrorCode.USER_EXIST);
        }

        if (!"ROLE_STUDENT".equals(dto.getRoleCode()) && !"ROLE_TEACHER".equals(dto.getRoleCode())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "Only ROLE_STUDENT/ROLE_TEACHER can register");
        }

        Role role = roleRepository.findByCode(dto.getRoleCode())
            .orElseThrow(() -> new BusinessException(ErrorCode.PARAM_ERROR.getCode(), dto.getRoleCode() + " not found"));

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setRealName(dto.getRealName());
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setStatus(1);
        user.setRoles(Set.of(role));
        userRepository.save(user);

        return Result.success();
    }

    @PutMapping("/password")
    public Result<Void> changePassword(@AuthenticationPrincipal CustomUserDetails principal,
                                       @Valid @RequestBody ChangePasswordDto dto) {
        User user = userRepository.findByUsername(principal.getUsername())
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXIST));

        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.PASSWORD_ERROR);
        }

        user.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
        return Result.success();
    }
}
