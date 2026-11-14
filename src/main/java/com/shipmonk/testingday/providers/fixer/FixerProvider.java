package com.shipmonk.testingday.providers.fixer;

import static com.shipmonk.testingday.utils.Consts.DATE_FORMAT;
import static java.net.URI.create;
import static java.net.http.HttpRequest.newBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.shipmonk.testingday.exceptions.ProviderRateException;
import com.shipmonk.testingday.providers.api.RatesProvider;
import com.shipmonk.testingday.providers.fixer.model.FixerResponse;

import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
public class FixerProvider implements RatesProvider {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public FixerProvider(
        @Value("${exchange.rates.providers.http-timeout-sec:1}") String httpTimeout,
        ObjectMapper objectMapper
    ) {
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(Integer.parseInt(httpTimeout)))
            .build();
        this.objectMapper = objectMapper;
    }

    @Value("${exchange.rates.providers.fixer.latest-url:https://data.fixer.io/api/latest}")
    private String fixerRatesLatestUrl;

    @Value("${exchange.rates.providers.fixer.date-url:https://data.fixer.io/api}")
    private String fixerRatesDateUrl;

    @Value("${exchange.rates.providers.fixer.api-key}")
    private String fixerApiKey;

    @Value("${exchange.rates.providers.fixer.base-currency:EUR}")
    private String fixerBaseCurrency;

    @Override
    public String getName() {
        return "fixer.io";
    }

    @Override
    public Map<String, BigDecimal> getRates() throws ProviderRateException {
        HttpRequest request = newBuilder()
            .uri(create("%s?access_key=%s&base=%s".formatted(fixerRatesLatestUrl, fixerApiKey, fixerBaseCurrency)))
            .build();

        log.debug("Provider [{}] fetching latest rates at [{}]", getName(), fixerRatesDateUrl);
        return execRequest(request).getRates();
    }

    @Override
    public Map<String, BigDecimal> getRates(LocalDate date) throws ProviderRateException {
        HttpRequest request = newBuilder()
            .uri(create("%s/%s?access_key=%s&base=%s".formatted(
                fixerRatesDateUrl,
                date.format(DateTimeFormatter.ofPattern(DATE_FORMAT)),
                fixerApiKey,
                fixerBaseCurrency
            )))
            .build();

        log.debug("Provider [{}] fetching rates for [{}] at [{}]", getName(), date, fixerRatesDateUrl);
        return execRequest(request).getRates();
    }

    private FixerResponse execRequest(HttpRequest request) {
        HttpResponse<InputStream> response;

        try {
            response = httpClient.send(request, BodyHandlers.ofInputStream());
        } catch (IOException | InterruptedException e) {
            log.error("Failed to fetch rates via [{}]", getName());
            throw new ProviderRateException(getName(), e);
        }

        FixerResponse fixerResponse = objectMapper.readValue(response.body(), FixerResponse.class);

        if (response.statusCode() != 200 || !fixerResponse.getSuccess()) {
            log.error("Provider [{}] returned [{}] code: {}", getName(), response.statusCode(), fixerResponse);
            throw new ProviderRateException(getName(), fixerResponse.toString());
        }

        log.info("Provider [{}] fetched: {}", getName(), fixerResponse);
        return fixerResponse;
    }

}
