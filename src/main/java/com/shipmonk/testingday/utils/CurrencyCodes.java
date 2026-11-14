package com.shipmonk.testingday.utils;

import static com.shipmonk.testingday.utils.Consts.CURRENCY_REGEX;

import java.util.Arrays;
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

}
