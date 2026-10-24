package br.com.forjacode.shortifyapi.service.impl;

import br.com.forjacode.shortifyapi.exception.InvalidUrlException;
import br.com.forjacode.shortifyapi.exception.ShortUrlNotFoundException;
import br.com.forjacode.shortifyapi.model.ShortUrl;
import br.com.forjacode.shortifyapi.repository.ShortUrlRepository;
import br.com.forjacode.shortifyapi.service.ShortCodeGenerator;
import br.com.forjacode.shortifyapi.service.ShortUrlService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;

@Service
public class ShortUrlServiceImpl implements ShortUrlService {

    private final ShortUrlRepository repository;
    private final ShortCodeGenerator codeGenerator;

    public ShortUrlServiceImpl(ShortUrlRepository repository, ShortCodeGenerator codeGenerator) {
        this.repository = repository;
        this.codeGenerator = codeGenerator;
    }

    @Override
    public ShortUrl create(String originalUrl) {
        validate(originalUrl);
        String shortCode;
        do {
            shortCode = codeGenerator.generate();
        } while (repository.existsByShortCode(shortCode));
        return repository.save(new ShortUrl(originalUrl, shortCode));
    }

    @Override
    public ShortUrl findByShortCode(String shortCode) {
        return repository.findByShortCode(shortCode).orElseThrow(() -> new ShortUrlNotFoundException(shortCode));
    }

    @Override
    @Transactional
    public ShortUrl resolveAndRegisterAccess(String shortCode) {
        ShortUrl shortUrl = findByShortCode(shortCode);
        repository.incrementAccessCount(shortUrl.getId());
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
