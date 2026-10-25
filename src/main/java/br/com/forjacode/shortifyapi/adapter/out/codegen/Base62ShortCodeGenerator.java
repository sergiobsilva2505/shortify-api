package br.com.forjacode.shortifyapi.adapter.out.codegen;

import br.com.forjacode.shortifyapi.application.port.out.ShortCodeGeneratorPort;
import br.com.forjacode.shortifyapi.domain.model.ShortCodeFormat;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class Base62ShortCodeGenerator implements ShortCodeGeneratorPort {

    private static final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    private final SecureRandom random = new SecureRandom();

    @Override
    public String generate() {
        StringBuilder code = new StringBuilder(ShortCodeFormat.LENGTH);
        for (int i = 0; i < ShortCodeFormat.LENGTH; i++) {
            code.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return code.toString();
    }
}
