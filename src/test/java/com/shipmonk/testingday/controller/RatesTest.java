package com.shipmonk.testingday.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.shipmonk.testingday.entity.RateEntity;
import com.shipmonk.testingday.entity.RatesUpdateHistEntity;
import com.shipmonk.testingday.providers.api.ProviderRates;

class RatesTest extends BaseTest {

    private static final LocalDateTime AT = LocalDateTime.of(2024, 1, 15, 10, 0);
    private static final String BASE = "EUR";

    @Test
    void getRates_returnsLatestCachedRates() throws Exception {
        when(historyRepo.findLatest()).thenReturn(Optional.of(snapshot()));
        when(rateRepo.findAllByHist(any())).thenReturn(List.of(rate("USD", "1.1"), rate("GBP", "0.85")));

        mockMvc.perform(get("/api/v1/rates"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.base").value(BASE))
            .andExpect(jsonPath("$.rates.USD").value(1.1))
            .andExpect(jsonPath("$.rates.GBP").value(0.85));
    }

    @Test
    void getRates_filtersByRequestedCodes() throws Exception {
        when(historyRepo.findLatest()).thenReturn(Optional.of(snapshot()));
        when(rateRepo.findAllByHistAndCodeIn(any(), eq(Set.of("USD")))).thenReturn(List.of(rate("USD", "1.1")));

        mockMvc.perform(get("/api/v1/rates").param("codes", "usd"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.rates.USD").value(1.1))
            .andExpect(jsonPath("$.rates.GBP").doesNotExist());
    }

    @Test
    void getRatesForDate_notCached_fetchesFromProviderAndStores() throws Exception {
        LocalDate date = LocalDate.of(2024, 1, 15);
        when(historyRepo.findLatestBetween(any(), any())).thenReturn(Optional.empty());
        when(fixerProvider.getRates(date)).thenReturn(new ProviderRates(BASE, Map.of("USD", new BigDecimal("1.1"))));

        mockMvc.perform(get("/api/v1/rates/2024-01-15"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.base").value(BASE))
            .andExpect(jsonPath("$.rates.USD").value(1.1));

        verify(fixerProvider).getRates(date);
        verify(rateRepo).saveAll(anyList());
        verify(historyRepo).save(argThat(hist -> hist.getSuccess() && BASE.equals(hist.getBase())));
    }

    @Test
    void getRatesForDate_invalidDateFormat_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/rates/2024-13-45"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.message").value(containsString("parameter 'date'")));
    }

    @Test
    void getRatesForDate_futureDate_returnsBadRequest() throws Exception {
        String tomorrow = LocalDate.now().plusDays(1).toString();

        mockMvc.perform(get("/api/v1/rates/" + tomorrow))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.message").value(containsString("cannot point in future")));
    }

    @Test
    void getRates_nothingCached_returnsNotFound() throws Exception {
        when(historyRepo.findLatest()).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/rates"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false));
    }

    private static RatesUpdateHistEntity snapshot() {
        return RatesUpdateHistEntity.builder()
            .success(true)
            .base(BASE)
            .at(AT)
            .timestamp(AT)
            .build();
    }

    private static RateEntity rate(String code, String rate) {
        return RateEntity.builder()
            .code(code)
            .rate(new BigDecimal(rate))
            .hist(snapshot())
            .build();
    }

}
