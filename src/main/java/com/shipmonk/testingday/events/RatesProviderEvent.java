package com.shipmonk.testingday.events;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RatesProviderEvent {

    @JsonProperty("provider_name")
    private String providerName;
    private Boolean active;
    private String message;

}
