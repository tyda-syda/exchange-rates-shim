package com.shipmonk.testingday.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shipmonk.testingday.model.Response;
import com.shipmonk.testingday.service.RatesProviderService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/providers")
@RequiredArgsConstructor
public class ProvidersController {

    private final RatesProviderService providerService;

    @GetMapping
    public Response getAllProviders() {
        return makeResponse().providers(providerService.getProviders()).build();
    }

    @PutMapping("/{name}/disable")
    public Response disableProvider(@PathVariable("name") String name) {
        return makeResponse()
            .providers(List.of(providerService.updateProvider(name, false)))
            .build();
    }

    @PutMapping("/{name}/enable")
    public Response enableProvider(@PathVariable("name") String name) {
        return makeResponse()
            .providers(List.of(providerService.updateProvider(name, true)))
            .build();
    }

    private Response.ResponseBuilder makeResponse() {
        return Response.builder()
            .timestamp(LocalDateTime.now())
            .success(true);
    }

}
