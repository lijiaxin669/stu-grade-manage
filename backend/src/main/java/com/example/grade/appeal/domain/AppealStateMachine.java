package com.example.grade.appeal.domain;

import com.example.grade.common.ErrorCode;
import com.example.grade.exception.BusinessException;

import java.util.Map;
import java.util.Set;

public final class AppealStateMachine {

    private static final Map<AppealStatus, Set<AppealStatus>> TRANSITIONS = Map.of(
        AppealStatus.PENDING, Set.of(AppealStatus.UNDER_REVIEW),
        AppealStatus.UNDER_REVIEW, Set.of(AppealStatus.APPROVED, AppealStatus.REJECTED),
        AppealStatus.REJECTED, Set.of(AppealStatus.ARBITRATING),
        AppealStatus.ARBITRATING, Set.of(AppealStatus.APPROVED, AppealStatus.REJECTED),
        AppealStatus.APPROVED, Set.of(AppealStatus.CLOSED)
    );

    private AppealStateMachine() {
    }

    public static void validate(AppealStatus from, AppealStatus to) {
        Set<AppealStatus> allowed = TRANSITIONS.get(from);
        if (allowed == null || !allowed.contains(to)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(),
                "非法状态转移: " + from + " → " + to);
        }
    }

    public static boolean canTransit(AppealStatus from, AppealStatus to) {
        Set<AppealStatus> allowed = TRANSITIONS.get(from);
        return allowed != null && allowed.contains(to);
    }
}
