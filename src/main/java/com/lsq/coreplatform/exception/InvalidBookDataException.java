package com.lsq.coreplatform.exception;

public class InvalidBookDataException extends RuntimeException {
    private final String field;
    private final String message;
    public InvalidBookDataException(String field, String message) {
        super(field + ": " + message);
        this.field = field;
        this.message = message;
    }
    public String getField() { return field; }
    public String getValidationMessage() { return message; }
}

