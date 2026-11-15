package com.shipmonk.testingday.providers.api;

import java.math.BigDecimal;
import java.util.Map;

public record ProviderRates(String base, Map<String, BigDecimal> rates) {
}
