package com.nazir.urlshortener.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.GONE)  // 410
public class ShortUrlExpiredException extends RuntimeException {
    public ShortUrlExpiredException(String message) {
        super(message);
    }
}
