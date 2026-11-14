package com.shipmonk.testingday.providers.fixer.model;

import java.math.BigDecimal;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FixerResponse {

    private Boolean success;
    private String base;
    private FixerErrorResponse error;
    private Map<String, BigDecimal> rates;

}
