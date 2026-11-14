package com.shipmonk.testingday.exceptions;

public class RatesNotFoundException extends RuntimeException {

    public RatesNotFoundException() {
    }

    public RatesNotFoundException(String message) {
        super(message);
    }

}
