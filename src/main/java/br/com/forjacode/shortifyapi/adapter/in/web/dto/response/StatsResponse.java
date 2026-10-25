package br.com.forjacode.shortifyapi.adapter.in.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Estatísticas de acesso de uma URL curta")
public record StatsResponse(
        @Schema(description = "Código curto", example = "abc1234") String code,
        @Schema(description = "Quantidade de acessos (redirecionamentos)", example = "42") Long accessCount) {
}
