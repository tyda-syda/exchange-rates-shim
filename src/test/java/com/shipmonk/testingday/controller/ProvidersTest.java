package com.shipmonk.testingday.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.shipmonk.testingday.events.api.EventType;

class ProvidersTest extends BaseTest {

    @Test
    void getAllProviders_returnsRegisteredProviders() throws Exception {
        when(providerRepo.findAll()).thenReturn(List.of(provider(true)));

        mockMvc.perform(get("/api/v1/providers"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.providers[0].name").value(PROVIDER_NAME))
            .andExpect(jsonPath("$.providers[0].active").value(true));
    }

    @Test
    void disableProvider_deactivatesAndPublishesEvent() throws Exception {
        mockMvc.perform(put("/api/v1/providers/{name}/disable", PROVIDER_NAME))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.providers[0].name").value(PROVIDER_NAME))
            .andExpect(jsonPath("$.providers[0].active").value(false));

        verify(eventsPublisher).publish(eq(EventType.PROVIDER_DISABLED), any());
    }

    @Test
    void disableProvider_unknownProvider_returnsError() throws Exception {
        when(providerRepo.findByName("unknown")).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/v1/providers/{name}/disable", "unknown"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.message").value("Provider [unknown] not found"));
    }

    @Test
    void disableProvider_wrongHttpMethod_returnsMethodNotAllowed() throws Exception {
        mockMvc.perform(get("/api/v1/providers/{name}/disable", PROVIDER_NAME))
            .andExpect(status().isMethodNotAllowed())
            .andExpect(jsonPath("$.success").value(false));
    }

}
