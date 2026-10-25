package br.com.forjacode.shortifyapi.adapter.in.web;

import br.com.forjacode.shortifyapi.application.port.in.ShortUrlUseCase;
import br.com.forjacode.shortifyapi.domain.model.ShortCodeFormat;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Redirect", description = "Redirecionamento a partir do código curto")
public class ShortUrlRedirectController {

    private final ShortUrlUseCase service;

    public ShortUrlRedirectController(ShortUrlUseCase service) {
        this.service = service;
    }

    @Operation(summary = "Redireciona para a URL original e registra o acesso")
    @ApiResponse(responseCode = "302", description = "Redirecionamento para a URL original (header Location)")
    @ApiResponse(responseCode = "404", description = "Código não encontrado",
            content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class)))
    @GetMapping("/{code:" + ShortCodeFormat.REGEX + "}")
    public ResponseEntity<Void> redirect(
            @Parameter(description = "Código curto (7 caracteres alfanuméricos)", example = "abc1234")
            @PathVariable String code) {
        String target = service.resolveAndRegisterAccess(code).getOriginalUrl();
        return ResponseEntity.status(HttpStatus.FOUND).header(HttpHeaders.LOCATION, target).build();
    }
}
