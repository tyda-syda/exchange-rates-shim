package com.shipmonk.testingday.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.stereotype.Component;

import com.shipmonk.testingday.providers.api.RatesProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RatesFetcher {

    private final RatesProviderService providerService;
    private final RatesUpdateHistoryService updateHistory;

    public Optional<FetchedRates> fetchLatest() {
        return fetch(RatesProvider::getRates);
    }

    public Optional<FetchedRates> fetchForDate(LocalDate date) {
        return fetch(provider -> provider.getRates(date));
    }

    private Optional<FetchedRates> fetch(Function<RatesProvider, Map<String, BigDecimal>> call) {
        for (RatesProvider provider : providerService.getActiveProviders()) {
            try {
                log.info("Fetching rates via [{}]", provider.getName());
                Map<String, BigDecimal> rates = call.apply(provider);
                log.info("Fetched new rates via [{}]", provider.getName());

                return Optional.of(new FetchedRates(provider.getName(), rates));
            } catch (Exception e) {
                log.error("Fetching rates failed [{}]", provider.getName(), e);
                saveFail(provider, e);
            }
        }

        log.error("Failed to fetch rates across all active providers");
        return Optional.empty();
    }

    private void saveFail(RatesProvider provider, Exception cause) {
        try {
            updateHistory.saveHistFail(provider.getName(), cause);
        } catch (Exception e) {
            log.error("Failed to record failure of provider [{}]", provider.getName(), e);
        }
    }

}
