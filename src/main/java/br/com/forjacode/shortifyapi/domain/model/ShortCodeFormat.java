package br.com.forjacode.shortifyapi.domain.model;

public final class ShortCodeFormat {

    public static final int LENGTH = 7;

    // Formato dos códigos gerados: LENGTH caracteres alfanuméricos (Base62).
    public static final String REGEX = "[0-9A-Za-z]{" + LENGTH + "}";

    private ShortCodeFormat() {
    }
}
