package br.com.forjacode.shortifyapi.service.impl;

import br.com.forjacode.shortifyapi.exception.InvalidUrlException;
import br.com.forjacode.shortifyapi.exception.ShortUrlNotFoundException;
import br.com.forjacode.shortifyapi.model.ShortUrl;
import br.com.forjacode.shortifyapi.repository.ShortUrlRepository;
import br.com.forjacode.shortifyapi.service.ShortCodeGenerator;
import br.com.forjacode.shortifyapi.service.ShortUrlService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;

@Service
public class ShortUrlServiceImpl implements ShortUrlService {

    private static final int MAX_CREATE_ATTEMPTS = 5;

    private final ShortUrlRepository repository;
    private final ShortCodeGenerator codeGenerator;

    public ShortUrlServiceImpl(ShortUrlRepository repository, ShortCodeGenerator codeGenerator) {
        this.repository = repository;
        this.codeGenerator = codeGenerator;
    }

    @Override
    public ShortUrl create(String originalUrl) {
        validate(originalUrl);
        // A checagem de existência não é atômica com o save: sob concorrência, outro request pode
        // gravar o mesmo código antes. A constraint única barra o duplicado e tentamos com outro código.
        for (int attempt = 1; ; attempt++) {
            try {
                return repository.save(new ShortUrl(originalUrl, generateAvailableCode()));
            } catch (DataIntegrityViolationException e) {
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
        return shortUrl;
    }

    @Override
    @Transactional
    public void delete(String code) {
        if (repository.deleteByShortCode(code) == 0) {
            throw new ShortUrlNotFoundException(code);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Long getAccessCount(String code) {
        return repository.findAccessCountByShortCode(code).orElseThrow(() -> new ShortUrlNotFoundException(code));
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
