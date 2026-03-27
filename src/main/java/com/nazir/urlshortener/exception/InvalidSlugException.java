package com.nazir.urlshortener.exception;

import lombok.Getter;

@Getter
public class InvalidSlugException extends RuntimeException {

    private final String slug;

    public InvalidSlugException(String message) {
        super(message);
        this.slug = null;
    }

    public InvalidSlugException(String slug, String reason) {
        super("Invalid slug [" + slug + "]: " + reason);
        this.slug = slug;
    }

}
