package br.com.forjacode.shortifyapi.application.service;

import br.com.forjacode.shortifyapi.application.port.in.ShortUrlUseCase;
import br.com.forjacode.shortifyapi.application.port.out.ShortCodeGeneratorPort;
import br.com.forjacode.shortifyapi.application.port.out.ShortUrlRepositoryPort;
import br.com.forjacode.shortifyapi.domain.exception.InvalidUrlException;
import br.com.forjacode.shortifyapi.domain.exception.ShortCodeAlreadyExistsException;
import br.com.forjacode.shortifyapi.domain.exception.ShortUrlNotFoundException;
import br.com.forjacode.shortifyapi.domain.model.ShortUrl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;

@Service
public class ShortUrlService implements ShortUrlUseCase {

    private static final int MAX_CREATE_ATTEMPTS = 5;

    private final ShortUrlRepositoryPort repository;
    private final ShortCodeGeneratorPort codeGenerator;

    public ShortUrlService(ShortUrlRepositoryPort repository, ShortCodeGeneratorPort codeGenerator) {
        this.repository = repository;
        this.codeGenerator = codeGenerator;
    }

    @Override
    public ShortUrl create(String originalUrl) {
        validate(originalUrl);
        for (int attempt = 1; ; attempt++) {
            try {
                return repository.save(new ShortUrl(originalUrl, generateAvailableCode()));
            } catch (ShortCodeAlreadyExistsException e) {
                if (attempt == MAX_CREATE_ATTEMPTS) {
                    throw e;
                }
            }
        }
    }

    private String generateAvailableCode() {
        String shortCode;
        do {
            shortCode = codeGenerator.generate();
        } while (repository.existsByShortCode(shortCode));
        return shortCode;
    }

    @Override
    public ShortUrl findByShortCode(String shortCode) {
        return repository.findByShortCode(shortCode).orElseThrow(() -> new ShortUrlNotFoundException(shortCode));
    }

    @Override
    @Transactional
    public ShortUrl resolveAndRegisterAccess(String shortCode) {
        ShortUrl shortUrl = findByShortCode(shortCode);
        if (repository.incrementAccessCount(shortUrl.getId()) == 0) {
            throw new ShortUrlNotFoundException(shortCode);
        }
        return shortUrl;
    }

    @Override
    @Transactional
    public ShortUrl update(String code, String newOriginalUrl) {
        ShortUrl shortUrl = findByShortCode(code);
        validate(newOriginalUrl);
        shortUrl.updateOriginalUrl(newOriginalUrl);
        return repository.save(shortUrl);
    }

    @Override
    @Transactional
    public void delete(String code) {
        if (repository.deleteByShortCode(code) == 0) {
            throw new ShortUrlNotFoundException(code);
        }
    }

    private void validate(String url) {
        try {
            URI uri = URI.create(url);
            String scheme = uri.getScheme();
            if (uri.getHost() == null || !("http".equals(scheme) || "https".equals(scheme))) {
                throw new InvalidUrlException(url);
            }
        } catch (IllegalArgumentException e) {
            throw new InvalidUrlException(url);
        }
    }
}
