package com.example.grade.appeal.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class AppealApprovedEvent extends ApplicationEvent {
    private final Long appealId;
    private final Long gradeId;
    private final java.math.BigDecimal finalScore;
    private final Long operatorId;
    private final String operatorRole;

    public AppealApprovedEvent(Object source, Long appealId, Long gradeId, 
                               java.math.BigDecimal finalScore, Long operatorId, String operatorRole) {
        super(source);
        this.appealId = appealId;
        this.gradeId = gradeId;
        this.finalScore = finalScore;
        this.operatorId = operatorId;
        this.operatorRole = operatorRole;
    }
}
