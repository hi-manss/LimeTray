package com.api.order.exceptions;

public class ApiException extends RuntimeException {

    private final String errorCode;

    public ApiException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
