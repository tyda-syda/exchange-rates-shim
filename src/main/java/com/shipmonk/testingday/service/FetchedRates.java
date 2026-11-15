package com.shipmonk.testingday.service;

import java.math.BigDecimal;
import java.util.Map;

public record FetchedRates(String providerName, String base, Map<String, BigDecimal> rates) {
}
