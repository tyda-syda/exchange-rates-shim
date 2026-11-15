package com.shipmonk.testingday.utils;

import static com.shipmonk.testingday.utils.Consts.CURRENCY_REGEX;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.shipmonk.testingday.exceptions.MalformedRequestException;

public final class CurrencyCodes {

    private CurrencyCodes() {
    }

    public static Set<String> parse(String codes) {
        if (codes == null || codes.isBlank()) {
            return Set.of();
        }

        Set<String> parsedCodes = Arrays.stream(codes.split(","))
            .map(String::trim)
            .filter(code -> code.matches(CURRENCY_REGEX))
            .map(String::toUpperCase)
            .collect(Collectors.toSet());

        if (parsedCodes.isEmpty()) {
            throw new MalformedRequestException("None of the supplied currency codes are valid [%s]".formatted(codes));
        }

        return parsedCodes;
    }

    public static Map<String, BigDecimal> filterCodes(Map<String, BigDecimal> rates, Set<String> codes) {
        if (codes.isEmpty()) {
            return rates;
        }

        return rates.entrySet().stream()
            .filter(entry -> codes.contains(entry.getKey()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

}
