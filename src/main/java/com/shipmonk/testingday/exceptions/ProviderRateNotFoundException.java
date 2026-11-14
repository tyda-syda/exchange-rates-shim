package com.shipmonk.testingday.exceptions;

public class ProviderRateNotFoundException extends RuntimeException {

    public ProviderRateNotFoundException(String name) {
        super("Provider [%s] not found".formatted(name));
    }

}
