package com.shipmonk.testingday.controller;

import static com.shipmonk.testingday.utils.Consts.DATE_FORMAT;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.shipmonk.testingday.model.Response;
import com.shipmonk.testingday.service.RatesService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/rates")
@RequiredArgsConstructor
public class RatesController {

    private final RatesService ratesService;

    @GetMapping
    public Response getRates(
        @RequestParam(value = "codes", required = false) String codes
    ) {
        return makeResponse().rates(ratesService.getRates(codes)).build();
    }

    @GetMapping("/{date}")
    public Response getRate(
        @PathVariable("date") @DateTimeFormat(pattern = DATE_FORMAT) LocalDate date,
        @RequestParam(value = "codes", required = false) String codes
    ) {
        return makeResponse().rates(ratesService.getRatesForDate(date, codes)).build();
    }

    private Response.ResponseBuilder makeResponse() {
        return Response.builder()
            .success(true)
            .timestamp(LocalDateTime.now());
    }

}
