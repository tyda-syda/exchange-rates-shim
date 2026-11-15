package com.shipmonk.testingday.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;

import com.shipmonk.testingday.service.RatesUpdateQueueService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class RatesUpdateQueueCleanupJob implements Job {

    private final RatesUpdateQueueService queueService;

    @Override
    public void execute(JobExecutionContext context) {
        log.info("RatesUpdateCleanupJob started");
        queueService.cleanupStaleEntries();
        log.info("RatesUpdateCleanupJob ended");
    }
}
