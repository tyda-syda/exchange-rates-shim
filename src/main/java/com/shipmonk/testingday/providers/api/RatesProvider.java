package com.shipmonk.testingday.providers.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import com.shipmonk.testingday.exceptions.ProviderRateException;

public interface RatesProvider {

    String getName();

    Map<String, BigDecimal> getRates() throws ProviderRateException;

    Map<String, BigDecimal> getRates(LocalDate date) throws ProviderRateException;

    default int getPriority() {
        return 1;
    }

}
