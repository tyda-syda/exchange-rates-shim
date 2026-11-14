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
    BAD_REQUEST(101, "bad_request");

    final int code;
    final String type;
}
