package br.com.forjacode.shortifyapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

// O limite de 2048 acompanha a coluna original_url (VARCHAR(2048)) no changelog do Liquibase.
public record ShortUrlRequest(
        @NotBlank(message = "A URL é obrigatória")
        @Size(max = 2048, message = "A URL deve ter no máximo 2048 caracteres")
        @URL(message = "A URL informada não é válida")
        String url) {
}
