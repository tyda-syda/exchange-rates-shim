package com.shipmonk.testingday.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI exchangeRatesOpenApi() {
        return new OpenAPI().info(
            new Info()
                .title("Exchange Rates API")
                .description("Caching layer over 3rd-party exchange rates providers")
                .version("v1")
        );
    }

}
