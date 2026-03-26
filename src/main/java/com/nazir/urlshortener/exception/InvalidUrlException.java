package com.nazir.urlshortener.exception;

public class InvalidUrlException extends RuntimeException {

    public InvalidUrlException(String url) {
        super("Invalid URL: " + url);
    }

    public InvalidUrlException(String url, String reason) {
        super("Invalid URL [" + url + "]: " + reason);
    }
}
