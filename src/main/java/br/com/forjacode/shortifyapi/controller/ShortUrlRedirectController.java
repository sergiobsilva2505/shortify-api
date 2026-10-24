package br.com.forjacode.shortifyapi.controller;

import br.com.forjacode.shortifyapi.service.ShortUrlService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ShortUrlRedirectController {

    private final ShortUrlService service;

    public ShortUrlRedirectController(ShortUrlService service) {
        this.service = service;
    }

    // Formato do Base62ShortCodeGenerator: 7 caracteres alfanuméricos.
    @GetMapping("/{code:[0-9A-Za-z]{7}}")
    public ResponseEntity<Void> redirect(@PathVariable String code) {
        String target = service.resolveAndRegisterAccess(code).getOriginalUrl();
        return ResponseEntity.status(HttpStatus.FOUND).header(HttpHeaders.LOCATION, target).build();
    }
}
