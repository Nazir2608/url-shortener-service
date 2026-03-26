package com.nazir.urlshortener.exception;

public class SlugAlreadyExistsException extends RuntimeException {

    public SlugAlreadyExistsException(String slug) {
        super("Slug already taken: " + slug);
    }
}
