package com.example.grade.controller;

import com.example.grade.common.Result;
import com.example.grade.security.CustomUserDetails;
import com.example.grade.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    // Course roster (Teacher/Admin)
    @GetMapping
    @PreAuthorize("hasAnyRole('TEACHER', 'SUPER_ADMIN') and hasAuthority('API_ENROLL_LIST')")
    public Result<java.util.List<com.example.grade.dto.enrollment.EnrollmentDto>> listByCourse(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam Long courseId) {
        return Result.success(enrollmentService.listByCourse(user.getUserId(), courseId, user.getAuthorities()));
    }

    @PostMapping("/courses/{courseId}/students/{studentId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') and hasAuthority('API_ENROLL_ADD')")
    public Result<Void> addStudent(@AuthenticationPrincipal CustomUserDetails user,
                                   @PathVariable Long courseId,
                                   @PathVariable Long studentId) {
        enrollmentService.addStudent(user.getUserId(), courseId, studentId, user.getAuthorities());
        return Result.success();
    }

    @DeleteMapping("/courses/{courseId}/students/{studentId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') and hasAuthority('API_ENROLL_REMOVE')")
    public Result<Void> removeStudent(@AuthenticationPrincipal CustomUserDetails user,
                                      @PathVariable Long courseId,
                                      @PathVariable Long studentId) {
        enrollmentService.removeStudent(user.getUserId(), courseId, studentId, user.getAuthorities());
        return Result.success();
    }
}
