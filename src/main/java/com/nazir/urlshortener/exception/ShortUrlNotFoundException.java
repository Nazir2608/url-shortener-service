package com.nazir.urlshortener.exception;

public class ShortUrlNotFoundException extends RuntimeException {

    public ShortUrlNotFoundException(String slug) {
        super("Short URL not found for slug: " + slug);
    }
}
