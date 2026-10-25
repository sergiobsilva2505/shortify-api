package br.com.forjacode.shortifyapi.service;

public interface ShortCodeGenerator {

    int CODE_LENGTH = 7;

    // Formato dos códigos gerados: CODE_LENGTH caracteres alfanuméricos (Base62).
    String CODE_REGEX = "[0-9A-Za-z]{" + CODE_LENGTH + "}";

    String generate();
}
