package com.shipmonk.testingday.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Response {

    private Boolean success;
    private long timestamp;
    private ErrorResponse error;
    private String base;
    private Map<String, BigDecimal> rates;
    private List<ProviderResponse> providers;

}
