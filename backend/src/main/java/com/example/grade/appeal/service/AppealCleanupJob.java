package com.example.grade.appeal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppealCleanupJob {

    private final AppealService appealService;

    @Scheduled(cron = "0 0 3 * * *")
    public void closeExpiredRejected() {
        log.info("AppealCleanupJob: start closing REJECTED appeals older than 7 days");
        int count = appealService.closeRejectedOlderThan7Days();
        log.info("AppealCleanupJob: closed {} appeals", count);
    }
}
