package br.com.forjacode.shortifyapi.controller;

import br.com.forjacode.shortifyapi.dto.request.ShortUrlRequest;
import br.com.forjacode.shortifyapi.dto.response.ShortUrlResponse;
import br.com.forjacode.shortifyapi.dto.response.StatsResponse;
import br.com.forjacode.shortifyapi.mapper.ShortUrlMapper;
import br.com.forjacode.shortifyapi.service.ShortCodeGenerator;
import br.com.forjacode.shortifyapi.service.ShortUrlService;
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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/shorten")
public class ShortUrlController {

    private static final String CODE_PATH = "/{code:" + ShortCodeGenerator.CODE_REGEX + "}";

    private final ShortUrlService service;
    private final ShortUrlMapper mapper;

    public ShortUrlController(ShortUrlService service, ShortUrlMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<ShortUrlResponse> shorten(@Valid @RequestBody ShortUrlRequest request) {
        ShortUrlResponse response = mapper.toResponse(service.create(request.url()), currentBaseUrl());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping(CODE_PATH)
    public ResponseEntity<ShortUrlResponse> findByCode(@PathVariable String code) {
        ShortUrlResponse response = mapper.toResponse(service.findByShortCode(code), currentBaseUrl());
        return ResponseEntity.ok(response);
    }

    @PutMapping(CODE_PATH)
    public ResponseEntity<ShortUrlResponse> update(@PathVariable String code, @Valid @RequestBody ShortUrlRequest request) {
        ShortUrlResponse response = mapper.toResponse(service.update(code, request.url()), currentBaseUrl());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping(CODE_PATH)
    public ResponseEntity<Void> delete(@PathVariable String code) {
        service.delete(code);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(CODE_PATH + "/stats")
    public ResponseEntity<StatsResponse> getStats(@PathVariable String code) {
        StatsResponse response = mapper.toStatsResponse(code, service.getAccessCount(code));
        return ResponseEntity.ok(response);
    }

    private String currentBaseUrl() {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .build()
                .toUriString();
    }
}
