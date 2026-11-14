package com.shipmonk.testingday.exceptions;

public class MalformedRequestException extends RuntimeException {

    public MalformedRequestException(String message) {
        super(message);
    }

}
