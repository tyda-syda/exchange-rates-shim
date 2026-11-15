package com.shipmonk.testingday.providers.api;

import java.time.LocalDate;

import com.shipmonk.testingday.exceptions.ProviderRateException;

public interface RatesProvider {

    String getName();

    ProviderRates getRates() throws ProviderRateException;

    ProviderRates getRates(LocalDate date) throws ProviderRateException;

    default int getPriority() {
        return 1;
    }

}
