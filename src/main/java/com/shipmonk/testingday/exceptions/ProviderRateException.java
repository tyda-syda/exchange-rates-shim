package com.shipmonk.testingday.exceptions;

public class ProviderRateException extends RuntimeException {

    public ProviderRateException(String providerName, String message) {
        super("Provider [%s] failed: %s".formatted(providerName, message));
    }

    public ProviderRateException(String providerName, Throwable throwable) {
        super("Provider [%s] failed".formatted(providerName), throwable);
    }

}
