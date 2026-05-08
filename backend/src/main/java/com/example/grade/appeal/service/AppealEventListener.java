package com.example.grade.appeal.service;

import com.example.grade.appeal.domain.AppealStatus;
import com.example.grade.appeal.domain.GradeAppeal;
import com.example.grade.appeal.repository.GradeAppealRepository;
import com.example.grade.domain.Grade;
import com.example.grade.repository.GradeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppealEventListener {

    private final GradeAppealRepository gradeAppealRepository;
    private final GradeRepository gradeRepository;
    private final AppealAuditService appealAuditService;

    @EventListener
    @Transactional
    public void handleAppealApproved(AppealApprovedEvent event) {
        GradeAppeal appeal = gradeAppealRepository.findById(event.getAppealId()).orElse(null);
        if (appeal == null || appeal.getStatus() != AppealStatus.APPROVED) {
            log.warn("AppealApprovedEvent: appeal not found or not APPROVED, id={}", event.getAppealId());
            return;
        }

        try {
            Grade grade = gradeRepository.findById(event.getGradeId())
                .orElseThrow(() -> new RuntimeException("Grade not found: " + event.getGradeId()));
            grade.setScore(event.getFinalScore());
            grade.setUpdatedAt(LocalDateTime.now());
            gradeRepository.save(grade);

            AppealStatus from = appeal.getStatus();
            appeal.setStatus(AppealStatus.CLOSED);
            appeal.setClosedAt(LocalDateTime.now());
            appeal.setUpdatedAt(LocalDateTime.now());
            gradeAppealRepository.save(appeal);

            appealAuditService.writeAuditLog(appeal.getId(), from.name(), AppealStatus.CLOSED.name(), null, "SYSTEM", "成绩调整完成，申诉自动关闭", true, null);

            log.info("Appeal {} CLOSED after grade {} adjusted to {}", appeal.getId(), event.getGradeId(), event.getFinalScore());
        } catch (Exception e) {
            log.error("Failed to adjust grade for appeal {}, score rollback", event.getAppealId(), e);
            throw e;
        }
    }
}
