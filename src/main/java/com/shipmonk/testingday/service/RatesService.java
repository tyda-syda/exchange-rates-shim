package com.shipmonk.testingday.service;

import static com.shipmonk.testingday.utils.Consts.DATE_FORMAT;
import static com.shipmonk.testingday.utils.CurrencyCodes.filterCodes;
import static java.time.format.DateTimeFormatter.ofPattern;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
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

    public RatesSnapshot getRates(String codes) {
        return requireRates(ratesStore.findLatest(CurrencyCodes.parse(codes)));
    }

    public RatesSnapshot getRatesForDate(LocalDate date, String codes) {
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
        Optional<RatesSnapshot> cachedRates = ratesStore.findForDate(date, parsedCodes);

        if (cachedRates.isPresent()) {
            return requireRates(cachedRates.get());
        }

        queueService.enterQueue(date);
        cachedRates = ratesStore.findForDate(date, parsedCodes);

        if (cachedRates.isPresent()) {
            queueService.leaveQueue(date);
            return requireRates(cachedRates.get());
        }

        FetchedRates fetched = fetchAndStoreForDate(date);

        return requireRates(new RatesSnapshot(fetched.base(), filterCodes(fetched.rates(), parsedCodes)));
    }

    private RatesSnapshot requireRates(RatesSnapshot snapshot) {
        if (snapshot.rates().isEmpty()) {
            throw new RatesNotFoundException();
        }

        return snapshot;
    }

    public void updateRates() {
        if (updateHistory.isThrottled()) {
            log.warn("Minimal interval for rates update hasn't passed yet");
            return;
        }

        ratesFetcher.fetchLatest().ifPresent(fetched -> ratesStore.save(fetched, null));
    }

    private FetchedRates fetchAndStoreForDate(LocalDate date) {
        FetchedRates fetched = ratesFetcher.fetchForDate(date)
            .orElseThrow(() -> new RatesProvidersUnavailableException(date));

        ratesStore.save(fetched, date);

        return fetched;
    }

}
