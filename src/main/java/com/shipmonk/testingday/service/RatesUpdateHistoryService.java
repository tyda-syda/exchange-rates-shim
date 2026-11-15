package com.shipmonk.testingday.service;

import static java.time.temporal.ChronoUnit.SECONDS;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shipmonk.testingday.entity.RatesUpdateHistEntity;
import com.shipmonk.testingday.events.RatesProviderEvent;
import com.shipmonk.testingday.events.api.EventType;
import com.shipmonk.testingday.events.api.EventsPublisher;
import com.shipmonk.testingday.repo.RatesUpdateHistRepo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RatesUpdateHistoryService {

    private final RatesUpdateHistRepo historyRepo;
    private final RatesProviderService providerService;
    private final EventsPublisher eventsPublisher;

    @Value("#{${exchange.rates.min-interval-rates-update-sec:60*60*12}}")
    private Long minIntervalRatesUpdate;

    @Transactional
    public void saveHistSuccess(String providerName, LocalDateTime at, LocalDateTime timestamp) {
        historyRepo.save(
            RatesUpdateHistEntity.builder()
                .provider(providerService.getProviderEntity(providerName))
                .success(true)
                .at(at)
                .timestamp(timestamp)
                .build()
        );
    }

    @Transactional
    public void saveHistFail(String providerName, Exception cause) {
        LocalDateTime now = LocalDateTime.now();

        historyRepo.save(
            RatesUpdateHistEntity.builder()
                .provider(providerService.getProviderEntity(providerName))
                .success(false)
                .at(now)
                .timestamp(now)
                .build()
        );
        eventsPublisher.publish(
            EventType.RATES_PROVIDER_FAIL,
            RatesProviderEvent.builder()
                .providerName(providerName)
                .message(cause.getMessage())
                .build()
        );
    }

    public boolean lastUpdateSuccess() {
        return historyRepo.findLatestByTimestamp().isPresent();
    }

    public boolean isThrottled() {
        return historyRepo.findLatestByTimestamp()
            .map(last -> SECONDS.between(last.getTimestamp(), LocalDateTime.now()) < minIntervalRatesUpdate)
            .orElse(false);
    }

}
