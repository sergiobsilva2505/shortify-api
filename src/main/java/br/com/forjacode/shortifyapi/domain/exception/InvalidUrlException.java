package br.com.forjacode.shortifyapi.domain.exception;

public class InvalidUrlException extends RuntimeException {

    public InvalidUrlException(String url) {
        super("Invalid URL: " + url);
    }
}
