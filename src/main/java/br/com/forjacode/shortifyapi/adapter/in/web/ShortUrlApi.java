package br.com.forjacode.shortifyapi.adapter.in.web;

import br.com.forjacode.shortifyapi.adapter.in.web.dto.request.ShortUrlRequest;
import br.com.forjacode.shortifyapi.adapter.in.web.dto.response.ShortUrlResponse;
import br.com.forjacode.shortifyapi.adapter.in.web.dto.response.ShortUrlStatsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

@Tag(name = "Short URLs", description = "Criação, consulta, atualização, remoção e estatísticas de URLs curtas")
public interface ShortUrlApi {

    String CODE_DESCRIPTION = "Código curto (7 caracteres alfanuméricos)";
    String CODE_EXAMPLE = "abc1234";

    @Operation(summary = "Cria uma URL curta")
    @ApiResponse(responseCode = "201", description = "URL curta criada")
    @ApiResponse(responseCode = "400", description = "URL ausente ou inválida",
            content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class)))
    ResponseEntity<ShortUrlResponse> shorten(ShortUrlRequest request);

    @Operation(summary = "Consulta uma URL curta pelo código")
    @ApiResponse(responseCode = "200", description = "URL curta encontrada")
    @ApiResponse(responseCode = "404", description = "Código não encontrado",
            content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class)))
    ResponseEntity<ShortUrlResponse> findByCode(
            @Parameter(description = CODE_DESCRIPTION, example = CODE_EXAMPLE) String code);

    @Operation(summary = "Atualiza a URL original de um código curto")
    @ApiResponse(responseCode = "200", description = "URL curta atualizada")
    @ApiResponse(responseCode = "400", description = "URL ausente ou inválida",
            content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "Código não encontrado",
            content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class)))
    ResponseEntity<ShortUrlResponse> update(
            @Parameter(description = CODE_DESCRIPTION, example = CODE_EXAMPLE) String code,
            ShortUrlRequest request);

    @Operation(summary = "Remove uma URL curta")
    @ApiResponse(responseCode = "204", description = "URL curta removida")
    @ApiResponse(responseCode = "404", description = "Código não encontrado",
            content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class)))
    ResponseEntity<Void> delete(
            @Parameter(description = CODE_DESCRIPTION, example = CODE_EXAMPLE) String code);

    @Operation(summary = "Consulta as estatísticas de acesso de uma URL curta")
    @ApiResponse(responseCode = "200", description = "Estatísticas retornadas")
    @ApiResponse(responseCode = "404", description = "Código não encontrado",
            content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class)))
    ResponseEntity<ShortUrlStatsResponse> getStats(
            @Parameter(description = CODE_DESCRIPTION, example = CODE_EXAMPLE) String code);
}
