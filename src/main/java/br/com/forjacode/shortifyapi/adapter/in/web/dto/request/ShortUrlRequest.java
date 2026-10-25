package br.com.forjacode.shortifyapi.adapter.in.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

// O limite de 2048 acompanha a coluna original_url (VARCHAR(2048)) no changelog do Liquibase.
@Schema(description = "Dados para criar ou atualizar uma URL curta")
public record ShortUrlRequest(
        @Schema(description = "URL original (http ou https)", example = "https://example.com/artigo/longo", maxLength = 2048)
        @NotBlank(message = "A URL é obrigatória")
        @Size(max = 2048, message = "A URL deve ter no máximo 2048 caracteres")
        @URL(message = "A URL informada não é válida")
        String url) {
}
