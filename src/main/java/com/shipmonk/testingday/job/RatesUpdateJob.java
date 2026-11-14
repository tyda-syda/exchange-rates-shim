package com.shipmonk.testingday.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;

import com.shipmonk.testingday.service.RatesService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class RatesUpdateJob implements Job {

    private final RatesService ratesService;

    @Override
    public void execute(JobExecutionContext context) {
        log.info("RatesUpdateJob started");
        ratesService.updateRates();
        log.info("RatesUpdateJob finished");
    }

}
