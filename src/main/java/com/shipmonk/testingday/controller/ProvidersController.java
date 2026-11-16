package com.shipmonk.testingday.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shipmonk.testingday.model.Response;
import com.shipmonk.testingday.service.RatesProviderService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Providers", description = "Runtime configuration of rates providers")
@RestController
@RequestMapping("/api/v1/providers")
@RequiredArgsConstructor
public class ProvidersController {

    private final RatesProviderService providerService;

    @Operation(summary = "All registered providers")
    @GetMapping
    public Response getAllProviders() {
        return makeResponse().providers(providerService.getProviders()).build();
    }

    @Operation(summary = "Exclude provider from fetching rates")
    @ApiResponse(responseCode = "200", description = "Updated provider")
    @ApiResponse(
        responseCode = "404",
        description = "Unknown provider",
        content = @Content(schema = @Schema(implementation = Response.class))
    )
    @PutMapping("/{name}/disable")
    public Response disableProvider(
        @Parameter(description = "Provider name", example = "fixer.io") @PathVariable("name") String name
    ) {
        return makeResponse()
            .providers(List.of(providerService.updateProvider(name, false)))
            .build();
    }

    @Operation(summary = "Include provider into fetching rates")
    @ApiResponse(responseCode = "200", description = "Updated provider")
    @ApiResponse(
        responseCode = "404",
        description = "Unknown provider",
        content = @Content(schema = @Schema(implementation = Response.class))
    )
    @PutMapping("/{name}/enable")
    public Response enableProvider(
        @Parameter(description = "Provider name", example = "fixer.io") @PathVariable("name") String name
    ) {
        return makeResponse()
            .providers(List.of(providerService.updateProvider(name, true)))
            .build();
    }

    @Operation(summary = "Change provider priority, lower value is tried first")
    @ApiResponse(responseCode = "200", description = "Updated provider")
    @ApiResponse(
        responseCode = "400",
        description = "Negative or non-numeric priority",
        content = @Content(schema = @Schema(implementation = Response.class))
    )
    @ApiResponse(
        responseCode = "404",
        description = "Unknown provider",
        content = @Content(schema = @Schema(implementation = Response.class))
    )
    @PutMapping("/{name}/priority/{priority}")
    public Response updatePriority(
        @Parameter(description = "Provider name", example = "fixer.io") @PathVariable("name") String name,
        @Parameter(description = "New priority, not negative", example = "1") @PathVariable("priority") int priority
    ) {
        return makeResponse()
            .providers(List.of(providerService.updatePriority(name, priority)))
            .build();
    }

    private Response.ResponseBuilder makeResponse() {
        return Response.builder()
            .timestamp(System.currentTimeMillis())
            .success(true);
    }

}
