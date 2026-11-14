package com.shipmonk.testingday.service;

import java.math.BigDecimal;
import java.util.Map;

public record FetchedRates(String providerName, Map<String, BigDecimal> rates) {
}
