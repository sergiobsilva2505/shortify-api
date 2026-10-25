package br.com.forjacode.shortifyapi.controller;

import br.com.forjacode.shortifyapi.service.ShortCodeGenerator;
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

    @GetMapping("/{code:" + ShortCodeGenerator.CODE_REGEX + "}")
    public ResponseEntity<Void> redirect(@PathVariable String code) {
        String target = service.resolveAndRegisterAccess(code).getOriginalUrl();
        return ResponseEntity.status(HttpStatus.FOUND).header(HttpHeaders.LOCATION, target).build();
    }
}
