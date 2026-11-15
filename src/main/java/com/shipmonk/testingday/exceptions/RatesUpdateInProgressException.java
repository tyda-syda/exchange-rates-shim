package com.shipmonk.testingday.exceptions;

import static com.shipmonk.testingday.utils.Consts.DATE_FORMAT;
import static java.time.format.DateTimeFormatter.ofPattern;

import java.time.LocalDate;

public class RatesUpdateInProgressException extends RuntimeException {

    public RatesUpdateInProgressException(LocalDate date) {
        super("Rates update for [%s] is already queued".formatted(date.format(ofPattern(DATE_FORMAT))));
    }

}
