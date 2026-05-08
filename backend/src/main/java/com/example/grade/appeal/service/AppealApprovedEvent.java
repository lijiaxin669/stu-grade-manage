package com.example.grade.appeal.service;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class AppealApprovedEvent {
    private final Long appealId;
    private final Long gradeId;
    private final BigDecimal finalScore;

    public AppealApprovedEvent(Long appealId, Long gradeId, BigDecimal finalScore) {
        this.appealId = appealId;
        this.gradeId = gradeId;
        this.finalScore = finalScore;
    }
}
