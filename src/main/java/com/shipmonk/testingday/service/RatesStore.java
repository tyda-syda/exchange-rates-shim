package com.shipmonk.testingday.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.shipmonk.testingday.entity.RateEntity;
import com.shipmonk.testingday.entity.RatesUpdateHistEntity;
import com.shipmonk.testingday.events.RatesUpdateDoneEvent;
import com.shipmonk.testingday.exceptions.RatesNotFoundException;
import com.shipmonk.testingday.repo.RateRepo;
import com.shipmonk.testingday.repo.RatesUpdateHistRepo;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RatesStore {

    private final RateRepo rateRepo;
    private final RatesUpdateHistRepo histRepo;
    private final RatesUpdateHistoryService updatesHistService;
    private final ApplicationEventPublisher eventPublisher;

    public RatesSnapshot findLatest(Set<String> codes) {
        return findRatesAt(histRepo.findLatest().orElseThrow(RatesNotFoundException::new), codes);
    }

    public Optional<RatesSnapshot> findForDate(LocalDate date, Set<String> codes) {
        return histRepo.findLatestBetween(date.atStartOfDay(), date.plusDays(1).atStartOfDay())
            .map(latest -> findRatesAt(latest, codes));
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
        updatesHistService.saveHistSuccess(fetched.providerName(), fetched.base(), at, now);
    }

    private RatesSnapshot findRatesAt(RatesUpdateHistEntity hist, Set<String> codes) {
        List<RateEntity> rates = codes.isEmpty() ?
            rateRepo.findAllByAt(hist.getAt()) :
            rateRepo.findAllByAt(hist.getAt(), codes);

        return new RatesSnapshot(
            hist.getBase(),
            rates.stream().collect(Collectors.toMap(RateEntity::getCode, RateEntity::getRate))
        );
    }

}
