package com.example.grade.appeal.util;

import com.example.grade.appeal.domain.AppealStatus;
import com.example.grade.common.ErrorCode;
import com.example.grade.exception.BusinessException;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public class AppealStateMachine {

    private static final Map<AppealStatus, Set<AppealStatus>> VALID_TRANSITIONS = new EnumMap<>(AppealStatus.class);

    static {
        VALID_TRANSITIONS.put(AppealStatus.PENDING, EnumSet.of(AppealStatus.UNDER_REVIEW));
        VALID_TRANSITIONS.put(AppealStatus.UNDER_REVIEW, EnumSet.of(AppealStatus.APPROVED, AppealStatus.REJECTED));
        VALID_TRANSITIONS.put(AppealStatus.REJECTED, EnumSet.of(AppealStatus.ARBITRATING, AppealStatus.CLOSED));
        VALID_TRANSITIONS.put(AppealStatus.ARBITRATING, EnumSet.of(AppealStatus.APPROVED, AppealStatus.REJECTED));
        VALID_TRANSITIONS.put(AppealStatus.APPROVED, EnumSet.of(AppealStatus.CLOSED));
        VALID_TRANSITIONS.put(AppealStatus.CLOSED, EnumSet.noneOf(AppealStatus.class));
    }

    private AppealStateMachine() {
    }

    public static boolean isValidTransition(AppealStatus from, AppealStatus to) {
        Set<AppealStatus> allowed = VALID_TRANSITIONS.get(from);
        return allowed != null && allowed.contains(to);
    }

    public static void validateTransition(AppealStatus from, AppealStatus to) {
        if (!isValidTransition(from, to)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, 
                "非法状态转移: " + from + " → " + to);
        }
    }
}
