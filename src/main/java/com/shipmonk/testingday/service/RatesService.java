package com.shipmonk.testingday.service;

import static com.shipmonk.testingday.utils.Consts.DATE_FORMAT;
import static com.shipmonk.testingday.utils.CurrencyCodes.filterCodes;
import static com.shipmonk.testingday.utils.ExceptionUtils.isUniqueViolation;
import static java.time.format.DateTimeFormatter.ofPattern;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.shipmonk.testingday.exceptions.MalformedRequestException;
import com.shipmonk.testingday.exceptions.RatesInitializationException;
import com.shipmonk.testingday.exceptions.RatesNotFoundException;
import com.shipmonk.testingday.exceptions.RatesProvidersUnavailableException;
import com.shipmonk.testingday.utils.CurrencyCodes;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RatesService {

    private final RatesStore ratesStore;
    private final RatesFetcher ratesFetcher;
    private final RatesUpdateHistoryService updateHistory;
    private final RatesUpdateQueueService queueService;

    @Value("${exchange.rates.earliest-allowed-date}")
    private LocalDate earliestAllowedDate;

    @PostConstruct
    void init() {
        try {
            updateRates();
        } catch (Exception e) {
            log.error("Update rates failed", e);
        }

        if (!updateHistory.lastUpdateSuccess()) {
            throw new RatesInitializationException("Failed to fetch rates across all providers");
        }
    }

    public Map<String, BigDecimal> getRates(String codes) {
        Map<String, BigDecimal> rates = ratesStore.findLatest(CurrencyCodes.parse(codes));

        if (rates.isEmpty()) {
            throw new RatesNotFoundException();
        }

        return rates;
    }

    public Map<String, BigDecimal> getRatesForDate(LocalDate date, String codes) {
        if (date.isAfter(LocalDate.now())) {
            throw new MalformedRequestException(
                "Requested date [%s] cannot point in future".formatted(date.format(ofPattern(DATE_FORMAT)))
            );
        } else if (date.isBefore(earliestAllowedDate)) {
            throw new MalformedRequestException(
                "Requested date [%s] is too old, earliest allowed date [%s]".formatted(
                    date.format(ofPattern(DATE_FORMAT)),
                    earliestAllowedDate
                )
            );
        }

        Set<String> parsedCodes = CurrencyCodes.parse(codes);
        Optional<Map<String, BigDecimal>> cachedRates = ratesStore.findForDate(date, parsedCodes);

        if (cachedRates.isPresent()) {
            return requireRates(cachedRates.get());
        }

        queueService.enterQueue(date);
        cachedRates = ratesStore.findForDate(date, parsedCodes);

        if (cachedRates.isPresent()) {
            queueService.leaveQueue(date);
            return requireRates(cachedRates.get());
        }

        return requireRates(filterCodes(fetchAndStoreForDate(date), parsedCodes));
    }

    private Map<String, BigDecimal> requireRates(Map<String, BigDecimal> rates) {
        if (rates.isEmpty()) {
            throw new RatesNotFoundException();
        }

        return rates;
    }

    public void updateRates() {
        if (updateHistory.isThrottled()) {
            log.warn("Minimal interval for rates update hasn't passed yet");
            return;
        }

        storeRates(ratesFetcher.fetchLatest(), null);
    }

    private Map<String, BigDecimal> fetchAndStoreForDate(LocalDate date) {
        Optional<FetchedRates> fetched = ratesFetcher.fetchForDate(date);

        if (fetched.isEmpty()) {
            throw new RatesProvidersUnavailableException(date);
        }

        return storeRates(fetched, date);
    }

    private Map<String, BigDecimal> storeRates(Optional<FetchedRates> fetched, LocalDate date) {
        if (fetched.isEmpty()) {
            return Map.of();
        }

        try {
            ratesStore.save(fetched.get(), date);
        } catch (DataIntegrityViolationException e) {
            if (!isUniqueViolation(e)) {
                throw e;
            }

            log.debug("Fetched rates for [{}] already exist", date, e);
        }

        return fetched.get().rates();
    }

}
