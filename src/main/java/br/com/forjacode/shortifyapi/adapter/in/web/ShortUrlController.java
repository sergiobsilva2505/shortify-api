package br.com.forjacode.shortifyapi.adapter.in.web;

import br.com.forjacode.shortifyapi.adapter.in.web.dto.request.ShortUrlRequest;
import br.com.forjacode.shortifyapi.adapter.in.web.dto.response.ShortUrlResponse;
import br.com.forjacode.shortifyapi.adapter.in.web.dto.response.ShortUrlStatsResponse;
import br.com.forjacode.shortifyapi.adapter.in.web.mapper.ShortUrlMapper;
import br.com.forjacode.shortifyapi.application.port.in.ShortUrlUseCase;
import br.com.forjacode.shortifyapi.domain.model.ShortCodeFormat;
import br.com.forjacode.shortifyapi.domain.model.ShortUrl;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/shorten")
public class ShortUrlController implements ShortUrlApi {

    private static final String CODE_PATH = "/{code:" + ShortCodeFormat.REGEX + "}";

    private final ShortUrlUseCase service;
    private final ShortUrlMapper mapper;

    public ShortUrlController(ShortUrlUseCase service, ShortUrlMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @Override
    @PostMapping
    public ResponseEntity<ShortUrlResponse> shorten(@Valid @RequestBody ShortUrlRequest request) {
        ShortUrlResponse response = mapper.toResponse(service.create(request.url()));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    @GetMapping(CODE_PATH)
    public ResponseEntity<ShortUrlResponse> findByCode(@PathVariable String code) {
        ShortUrlResponse response = mapper.toResponse(service.resolveAndRegisterAccess(code));
        return ResponseEntity.ok(response);
    }

    @Override
    @PutMapping(CODE_PATH)
    public ResponseEntity<ShortUrlResponse> update(
            @PathVariable String code,
            @Valid @RequestBody ShortUrlRequest request) {
        ShortUrlResponse response = mapper.toResponse(service.update(code, request.url()));
        return ResponseEntity.ok(response);
    }

    @Override
    @DeleteMapping(CODE_PATH)
    public ResponseEntity<Void> delete(@PathVariable String code) {
        service.delete(code);
        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping(CODE_PATH + "/stats")
    public ResponseEntity<ShortUrlStatsResponse> getStats(@PathVariable String code) {
        ShortUrl shortUrl = service.findByShortCode(code);
        ShortUrlStatsResponse response = mapper.toStatsResponse(shortUrl);
        return ResponseEntity.ok(response);
    }
}
