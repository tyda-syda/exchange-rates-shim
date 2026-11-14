package com.shipmonk.testingday.config;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.shipmonk.testingday.job.RatesUpdateJob;

@Configuration
public class CronConfig {

    private static final String RATES_UPDATE_IDENTITY = "ratesUpdateJob";

    @Value("${exchange.rates.update-cron}")
    private String ratesUpdateCron;

    @Bean
    public JobDetail ratesUpdateJob() {
        return JobBuilder.newJob(RatesUpdateJob.class)
            .withIdentity(RATES_UPDATE_IDENTITY)
            .storeDurably()
            .build();
    }

    @Bean
    public Trigger ratesUpdateTrigger() {
        return TriggerBuilder.newTrigger()
            .forJob(RATES_UPDATE_IDENTITY)
            .withSchedule(CronScheduleBuilder.cronSchedule(ratesUpdateCron))
            .startNow()
            .build();
    }

}
