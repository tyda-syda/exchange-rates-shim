package com.shipmonk.testingday.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.shipmonk.testingday.entity.RateEntity;
import com.shipmonk.testingday.events.RatesUpdateDoneEvent;
import com.shipmonk.testingday.exceptions.RatesNotFoundException;
import com.shipmonk.testingday.repo.RateRepo;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RatesStore {

    private final RateRepo rateRepo;
    private final RatesUpdateHistoryService updateHistory;
    private final ApplicationEventPublisher eventPublisher;

    public Map<String, BigDecimal> findLatest(Set<String> codes) {
        RateEntity latest = rateRepo.findLatest().orElseThrow(RatesNotFoundException::new);

        return findRatesAt(latest.getAt(), codes);
    }

    public Optional<Map<String, BigDecimal>> findForDate(LocalDate date, Set<String> codes) {
        return rateRepo.findLatestBetween(date.atStartOfDay(), date.plusDays(1).atStartOfDay())
            .map(latest -> findRatesAt(latest.getAt(), codes));
    }

    @Transactional
    public void save(FetchedRates fetched, LocalDate date) {
        // only updates for a specific date must be queued
        if (date != null) {
            eventPublisher.publishEvent(new RatesUpdateDoneEvent(date));
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime at = date == null ? now : date.atTime(now.toLocalTime());
        List<RateEntity> newRates = fetched.rates().entrySet().stream()
            .map(entry ->
                RateEntity.builder()
                    .code(entry.getKey())
                    .rate(entry.getValue())
                    .at(at)
                    .build()
            )
            .toList();

        rateRepo.saveAll(newRates);
        updateHistory.saveHistSuccess(fetched.providerName(), at, now);
    }

    private Map<String, BigDecimal> findRatesAt(LocalDateTime at, Set<String> codes) {
        List<RateEntity> rates = codes.isEmpty() ? rateRepo.findAllByAt(at) : rateRepo.findAllByAt(at, codes);

        return rates.stream().collect(Collectors.toMap(RateEntity::getCode, RateEntity::getRate));
    }

}
