package com.shipmonk.testingday.controller;

import static com.shipmonk.testingday.utils.Consts.DATE_FORMAT;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.shipmonk.testingday.model.Response;
import com.shipmonk.testingday.service.RatesService;
import com.shipmonk.testingday.service.RatesSnapshot;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Rates", description = "Cached exchange rates")
@RestController
@RequestMapping("/api/v1/rates")
@RequiredArgsConstructor
public class RatesController {

    private final RatesService ratesService;

    @Operation(summary = "Latest cached rates")
    @ApiResponse(responseCode = "200", description = "Non-empty rates")
    @ApiResponse(
        responseCode = "400",
        description = "None of the supplied currency codes is valid",
        content = @Content(schema = @Schema(implementation = Response.class))
    )
    @ApiResponse(
        responseCode = "404",
        description = "No rates cached or none of the requested codes present",
        content = @Content(schema = @Schema(implementation = Response.class))
    )
    @GetMapping
    public Response getRates(
        @Parameter(description = "Comma separated, case-insensitive currency codes", example = "USD,GBP")
        @RequestParam(value = "codes", required = false) String codes
    ) {
        return toResponse(ratesService.getRates(codes));
    }

    @Operation(summary = "Rates for a specific day, fetched from providers and cached on first request")
    @ApiResponse(responseCode = "200", description = "Non-empty rates")
    @ApiResponse(
        responseCode = "400",
        description = "Malformed date, date in future or before the earliest allowed date, invalid currency codes",
        content = @Content(schema = @Schema(implementation = Response.class))
    )
    @ApiResponse(
        responseCode = "404",
        description = "None of the requested codes present",
        content = @Content(schema = @Schema(implementation = Response.class))
    )
    @ApiResponse(
        responseCode = "502",
        description = "All active providers failed to fetch rates",
        content = @Content(schema = @Schema(implementation = Response.class))
    )
    @ApiResponse(
        responseCode = "503",
        description = "Rates for this day are being fetched by another request, retry after Retry-After seconds",
        content = @Content(schema = @Schema(implementation = Response.class))
    )
    @GetMapping("/{date}")
    public Response getRate(
        @Parameter(description = "Day in format " + DATE_FORMAT, example = "2022-06-20")
        @PathVariable("date") @DateTimeFormat(pattern = DATE_FORMAT) LocalDate date,
        @Parameter(description = "Comma separated, case-insensitive currency codes", example = "USD,GBP")
        @RequestParam(value = "codes", required = false) String codes
    ) {
        return toResponse(ratesService.getRatesForDate(date, codes));
    }

    private Response toResponse(RatesSnapshot snapshot) {
        return Response.builder()
            .success(true)
            .timestamp(System.currentTimeMillis())
            .base(snapshot.base())
            .rates(snapshot.rates())
            .build();
    }

}
