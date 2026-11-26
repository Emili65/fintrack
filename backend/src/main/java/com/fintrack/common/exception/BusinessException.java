package com.fintrack.common.exception;

import org.springframework.http.HttpStatus;

/** Error de regla de negocio con el código HTTP que debe devolverse. */
public class BusinessException extends RuntimeException {

    private final HttpStatus status;

    public BusinessException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
