package com.nazir.urlshortener.exception;

public class ShortUrlExpiredException extends RuntimeException {

    public ShortUrlExpiredException(String slug) {
        super("Short URL has expired: " + slug);
    }
}
