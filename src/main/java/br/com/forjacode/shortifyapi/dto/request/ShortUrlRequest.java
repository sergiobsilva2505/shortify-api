package br.com.forjacode.shortifyapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

public record ShortUrlRequest(@NotBlank(message = "A URL é obrigatória") @URL(message = "A URL informada não é válida") String url) {
}
