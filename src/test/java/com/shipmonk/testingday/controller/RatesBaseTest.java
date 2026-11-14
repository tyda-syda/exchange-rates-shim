package com.shipmonk.testingday.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
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

class RatesBaseTest extends BaseTest {

    private static final LocalDateTime AT = LocalDateTime.of(2024, 1, 15, 10, 0);

    @Test
    void getRates_returnsLatestCachedRates() throws Exception {
        when(rateRepo.findLatest()).thenReturn(Optional.of(rate("USD", "1.1")));
        when(rateRepo.findAllByAt(AT)).thenReturn(List.of(rate("USD", "1.1"), rate("GBP", "0.85")));

        mockMvc.perform(get("/api/v1/rates"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.rates.USD").value(1.1))
            .andExpect(jsonPath("$.rates.GBP").value(0.85));
    }

    @Test
    void getRates_filtersByRequestedCodes() throws Exception {
        when(rateRepo.findLatest()).thenReturn(Optional.of(rate("USD", "1.1")));
        when(rateRepo.findAllByAt(AT, Set.of("USD"))).thenReturn(List.of(rate("USD", "1.1")));

        mockMvc.perform(get("/api/v1/rates").param("codes", "usd"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.rates.USD").value(1.1))
            .andExpect(jsonPath("$.rates.GBP").doesNotExist());
    }

    @Test
    void getRatesForDate_notCached_fetchesFromProviderAndStores() throws Exception {
        LocalDate date = LocalDate.of(2024, 1, 15);
        when(rateRepo.findLatestBetween(any(), any())).thenReturn(Optional.empty());
        when(fixerProvider.getRates(date)).thenReturn(Map.of("USD", new BigDecimal("1.1")));

        mockMvc.perform(get("/api/v1/rates/2024-01-15"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.rates.USD").value(1.1));

        verify(fixerProvider).getRates(date);
        verify(rateRepo).saveAll(anyList());
        verify(historyRepo).save(argThat(hist -> hist.getSuccess()));
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
        when(rateRepo.findLatest()).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/rates"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false));
    }

    private static RateEntity rate(String code, String rate) {
        return RateEntity.builder()
            .code(code)
            .rate(new BigDecimal(rate))
            .at(AT)
            .build();
    }

}
