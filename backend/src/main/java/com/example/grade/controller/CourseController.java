package com.example.grade.controller;

import com.example.grade.common.Result;
import com.example.grade.dto.course.CourseCreateDto;
import com.example.grade.dto.course.CourseDto;
import com.example.grade.dto.course.CourseUpdateDto;
import com.example.grade.security.CustomUserDetails;
import com.example.grade.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @GetMapping
    @PreAuthorize("hasAuthority('API_COURSE_LIST')")
    public Result<List<CourseDto>> list(@AuthenticationPrincipal CustomUserDetails user,
                                        @RequestParam(required = false) Long studentId) {
        return Result.success(courseService.listCourses(user, studentId));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('API_COURSE_CREATE')")
    public Result<Void> create(@AuthenticationPrincipal CustomUserDetails user, 
                               @Valid @RequestBody CourseCreateDto dto) {
        courseService.createCourse(user, dto);
        return Result.success();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('API_COURSE_UPDATE')")
    public Result<Void> update(@AuthenticationPrincipal CustomUserDetails user,
                               @PathVariable Long id,
                               @RequestBody CourseUpdateDto dto) {
        courseService.updateCourse(user, id, dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('API_COURSE_DELETE')")
    public Result<Void> delete(@AuthenticationPrincipal CustomUserDetails user,
                               @PathVariable Long id) {
        courseService.deleteCourse(user, id);
        return Result.success();
    }
}
