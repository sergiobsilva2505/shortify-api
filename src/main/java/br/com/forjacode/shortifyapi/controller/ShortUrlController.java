package br.com.forjacode.shortifyapi.controller;

import br.com.forjacode.shortifyapi.dto.request.ShortUrlRequest;
import br.com.forjacode.shortifyapi.dto.response.ShortUrlResponse;
import br.com.forjacode.shortifyapi.mapper.ShortUrlMapper;
import br.com.forjacode.shortifyapi.service.ShortUrlService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/shorten")
public class ShortUrlController {

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

    @GetMapping("/{code}/view")
    public ResponseEntity<ShortUrlResponse> findByCode(@PathVariable String code) {
        ShortUrlResponse response = mapper.toResponse(service.findByShortCode(code), currentBaseUrl());
        return ResponseEntity.ok(response);
    }

    private String currentBaseUrl() {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .build()
                .toUriString();
    }
}
