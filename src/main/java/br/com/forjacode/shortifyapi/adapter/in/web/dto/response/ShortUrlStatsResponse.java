package br.com.forjacode.shortifyapi.adapter.in.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Estatísticas de acesso de uma URL curta")
public record ShortUrlStatsResponse(
        @Schema(example = "1") Long id,
        @Schema(description = "URL original", example = "https://example.com/artigo/longo") String url,
        @Schema(description = "Código curto", example = "abc1234") String shortCode,
        Instant createdAt,
        Instant updatedAt,
        @Schema(description = "Quantidade de acessos (redirecionamentos)", example = "42") Long accessCount) {
}
