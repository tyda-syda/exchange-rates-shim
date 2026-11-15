package com.shipmonk.testingday.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum ErrorType {

    INTERNAL(0, "internal_error"),
    BAD_REQUEST(101, "bad_request"),
    RATES_UPDATE_IN_PROGRESS(102, "rates_update_in_progress"),
    RATES_PROVIDERS_UNAVAILABLE(103, "rates_providers_unavailable");

    final int code;
    final String type;
}
