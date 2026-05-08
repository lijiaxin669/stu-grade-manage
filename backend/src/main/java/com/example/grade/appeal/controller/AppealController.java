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
    public Result<AppealDto> create(@AuthenticationPrincipal CustomUserDetails user,
                                    @Valid @RequestBody AppealCreateDto dto) {
        return Result.success(appealService.createAppeal(user, dto));
    }

    @GetMapping("/mine")
    @PreAuthorize("hasAuthority('API_APPEAL_MINE')")
    public Result<List<AppealDto>> mine(@AuthenticationPrincipal CustomUserDetails user) {
        return Result.success(appealService.getMyAppeals(user));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAuthority('API_APPEAL_PENDING')")
    public Result<List<AppealDto>> pending(@AuthenticationPrincipal CustomUserDetails user) {
        return Result.success(appealService.getPendingAppeals(user));
    }

    @PostMapping("/{id}/claim")
    @PreAuthorize("hasAuthority('API_APPEAL_CLAIM')")
    public Result<AppealDto> claim(@AuthenticationPrincipal CustomUserDetails user,
                                   @PathVariable Long id) {
        return Result.success(appealService.claimAppeal(user, id));
    }

    @PostMapping("/{id}/decision")
    @PreAuthorize("hasAuthority('API_APPEAL_DECISION')")
    public Result<AppealDto> decision(@AuthenticationPrincipal CustomUserDetails user,
                                      @PathVariable Long id,
                                      @Valid @RequestBody AppealDecisionDto dto) {
        return Result.success(appealService.decision(user, id, dto));
    }

    @PostMapping("/{id}/arbitrate-request")
    @PreAuthorize("hasAuthority('API_APPEAL_ARBITRATE_REQUEST')")
    public Result<AppealDto> arbitrateRequest(@AuthenticationPrincipal CustomUserDetails user,
                                              @PathVariable Long id,
                                              @Valid @RequestBody ArbitrateRequestDto dto) {
        return Result.success(appealService.requestArbitration(user, id, dto));
    }

    @GetMapping("/arbitrating")
    @PreAuthorize("hasAuthority('API_APPEAL_ARBITRATING')")
    public Result<List<AppealDto>> arbitrating() {
        return Result.success(appealService.getArbitratingAppeals());
    }

    @PostMapping("/{id}/arbitrate")
    @PreAuthorize("hasAuthority('API_APPEAL_ARBITRATE')")
    public Result<AppealDto> arbitrate(@AuthenticationPrincipal CustomUserDetails user,
                                       @PathVariable Long id,
                                       @Valid @RequestBody ArbitrateDto dto) {
        return Result.success(appealService.arbitrate(user, id, dto));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('API_APPEAL_DETAIL')")
    public Result<AppealDto> detail(@AuthenticationPrincipal CustomUserDetails user,
                                    @PathVariable Long id) {
        return Result.success(appealService.getDetail(user, id));
    }

    @GetMapping("/{id}/audit-logs")
    @PreAuthorize("hasAuthority('API_APPEAL_AUDIT_LOGS')")
    public Result<List<AuditLogDto>> auditLogs(@PathVariable Long id) {
        return Result.success(appealService.getAuditLogs(id));
    }
}
