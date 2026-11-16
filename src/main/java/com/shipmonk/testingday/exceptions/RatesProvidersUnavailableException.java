package com.shipmonk.testingday.exceptions;

import static com.shipmonk.testingday.utils.Consts.DATE_FORMAT;
import static java.time.format.DateTimeFormatter.ofPattern;

import java.time.LocalDate;

public class RatesProvidersUnavailableException extends RuntimeException {

    public RatesProvidersUnavailableException() {
        super("At least 1 provider must be available");
    }

    public RatesProvidersUnavailableException(LocalDate date) {
        super("Failed to fetch rates for [%s] from all active providers".formatted(date.format(ofPattern(DATE_FORMAT))));
    }

}
