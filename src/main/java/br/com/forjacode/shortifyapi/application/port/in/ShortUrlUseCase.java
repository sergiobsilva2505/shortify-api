package br.com.forjacode.shortifyapi.application.port.in;

import br.com.forjacode.shortifyapi.domain.model.ShortUrl;

public interface ShortUrlUseCase {

    ShortUrl create(String originalUrl);

    ShortUrl findByShortCode(String shortCode);

    ShortUrl resolveAndRegisterAccess(String shortCode);

    ShortUrl update(String code, String newOriginalUrl);

    void delete(String code);

    Long getAccessCount(String code);
}
