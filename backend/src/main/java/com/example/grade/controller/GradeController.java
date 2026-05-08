package com.example.grade.controller;

import com.example.grade.common.Result;
import com.example.grade.dto.grade.CourseStatisticsDto;
import com.example.grade.dto.grade.GradeDto;
import com.example.grade.dto.grade.GradeCreateDto;
import com.example.grade.dto.grade.GradeUpdateDto;
import com.example.grade.security.CustomUserDetails;
import com.example.grade.service.GradeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/grades")
@RequiredArgsConstructor
public class GradeController {

    private final GradeService gradeService;

    @PostMapping
    @PreAuthorize("hasAuthority('API_GRADE_CREATE')")
    public Result<Void> input(@AuthenticationPrincipal CustomUserDetails user, 
                              @Valid @RequestBody GradeCreateDto dto) {
        gradeService.createGrade(user, dto);
        return Result.success();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('API_GRADE_UPDATE')")
    public Result<Void> update(@AuthenticationPrincipal CustomUserDetails user,
                               @PathVariable Long id,
                               @Valid @RequestBody GradeUpdateDto dto) {
        gradeService.updateGrade(user, id, dto);
        return Result.success();
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('API_GRADE_DELETE')")
    public Result<Void> delete(@AuthenticationPrincipal CustomUserDetails user,
                               @PathVariable Long id) {
        gradeService.deleteGrade(user, id);
        return Result.success();
    }

    @GetMapping
    @PreAuthorize("hasAuthority('API_GRADE_LIST')")
    public Result<List<GradeDto>> list(@AuthenticationPrincipal CustomUserDetails user,
                                       @RequestParam(required = false) Long courseId) {
        return Result.success(gradeService.listGrades(user.getUserId(), user.getAuthorities(), courseId));
    }
    
    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('API_GRADE_STATS')")
    public Result<CourseStatisticsDto> stats(@AuthenticationPrincipal CustomUserDetails user,
                                             @RequestParam Long courseId) {
        boolean isSuperAdmin = user.getAuthorities().contains(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_SUPER_ADMIN"));
        return Result.success(gradeService.getStats(courseId, user.getUserId(), isSuperAdmin));
    }

    @GetMapping("/export")
    @PreAuthorize("hasAuthority('API_GRADE_EXPORT')")
    public void export(@AuthenticationPrincipal CustomUserDetails user,
                       @RequestParam Long courseId,
                       jakarta.servlet.http.HttpServletResponse response) {
        boolean isSuperAdmin = user.getAuthorities().contains(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_SUPER_ADMIN"));
        gradeService.exportGrades(courseId, user.getUserId(), isSuperAdmin, response);
    }
}
