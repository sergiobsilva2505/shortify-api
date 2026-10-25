package br.com.forjacode.shortifyapi.domain.exception;

public class ShortCodeAlreadyExistsException extends RuntimeException {

    public ShortCodeAlreadyExistsException(String code) {
        super("Short code already exists: " + code);
    }
}
