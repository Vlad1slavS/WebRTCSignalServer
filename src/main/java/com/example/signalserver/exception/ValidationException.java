package com.example.signalserver.exception;

import lombok.Getter;

@Getter
public class ValidationException extends RuntimeException {

    private String field;
    private Object rejectedValue;

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String field, String message) {
        super(message);
        this.field = field;
    }

    public ValidationException(String field, Object rejectedValue, String message) {
        super(message);
        this.field = field;
        this.rejectedValue = rejectedValue;
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }

}
