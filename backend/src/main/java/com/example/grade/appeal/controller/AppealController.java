package com.example.grade.appeal.controller;

import com.example.grade.appeal.dto.*;
import com.example.grade.appeal.service.AppealService;
import com.example.grade.common.Result;
import com.example.grade.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/appeals")
@RequiredArgsConstructor
public class AppealController {

    private final AppealService appealService;

    @PostMapping
    @PreAuthorize("hasAuthority('API_APPEAL_CREATE')")
    public Result<Long> create(@AuthenticationPrincipal CustomUserDetails user,
                               @Valid @RequestBody AppealCreateDto dto) {
        return Result.success(appealService.createAppeal(user, dto));
    }

    @GetMapping("/mine")
    @PreAuthorize("hasAuthority('API_APPEAL_LIST_MINE')")
    public Result<List<AppealDto>> getMyAppeals(@AuthenticationPrincipal CustomUserDetails user) {
        return Result.success(appealService.getStudentAppeals(user));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAuthority('API_APPEAL_LIST_PENDING')")
    public Result<List<AppealDto>> getPending(@AuthenticationPrincipal CustomUserDetails user) {
        return Result.success(appealService.getTeacherPendingAppeals(user));
    }

    @PostMapping("/{id}/claim")
    @PreAuthorize("hasAuthority('API_APPEAL_CLAIM')")
    public Result<Void> claim(@AuthenticationPrincipal CustomUserDetails user,
                              @PathVariable Long id) {
        appealService.claimAppeal(user, id);
        return Result.success();
    }

    @PostMapping("/{id}/decision")
    @PreAuthorize("hasAuthority('API_APPEAL_DECIDE')")
    public Result<Void> decide(@AuthenticationPrincipal CustomUserDetails user,
                               @PathVariable Long id,
                               @Valid @RequestBody TeacherDecisionDto dto) {
        appealService.teacherDecision(user, id, dto);
        return Result.success();
    }

    @PostMapping("/{id}/arbitrate-request")
    @PreAuthorize("hasAuthority('API_APPEAL_REQUEST_ARBITRATE')")
    public Result<Void> requestArbitration(@AuthenticationPrincipal CustomUserDetails user,
                                           @PathVariable Long id,
                                           @Valid @RequestBody ArbitrateRequestDto dto) {
        appealService.requestArbitration(user, id, dto);
        return Result.success();
    }

    @GetMapping("/arbitrating")
    @PreAuthorize("hasAuthority('API_APPEAL_LIST_ARBITRATING')")
    public Result<List<AppealDto>> getArbitrating(@AuthenticationPrincipal CustomUserDetails user) {
        return Result.success(appealService.getArbitratingAppeals(user));
    }

    @PostMapping("/{id}/arbitrate")
    @PreAuthorize("hasAuthority('API_APPEAL_ARBITRATE')")
    public Result<Void> arbitrate(@AuthenticationPrincipal CustomUserDetails user,
                                  @PathVariable Long id,
                                  @Valid @RequestBody AdminArbitrateDto dto) {
        appealService.adminArbitrate(user, id, dto);
        return Result.success();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('API_APPEAL_DETAIL')")
    public Result<AppealDto> detail(@AuthenticationPrincipal CustomUserDetails user,
                                    @PathVariable Long id) {
        return Result.success(appealService.getAppealDetail(user, id));
    }

    @GetMapping("/{id}/audit-logs")
    @PreAuthorize("hasAuthority('API_APPEAL_AUDIT_LOGS')")
    public Result<List<AuditLogDto>> auditLogs(@AuthenticationPrincipal CustomUserDetails user,
                                                @PathVariable Long id) {
        return Result.success(appealService.getAuditLogs(user, id));
    }
}
