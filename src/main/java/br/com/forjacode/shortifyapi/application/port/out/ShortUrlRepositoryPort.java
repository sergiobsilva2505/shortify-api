package br.com.forjacode.shortifyapi.application.port.out;

import br.com.forjacode.shortifyapi.domain.model.ShortUrl;

import java.util.Optional;

public interface ShortUrlRepositoryPort {

    ShortUrl save(ShortUrl shortUrl);

    Optional<ShortUrl> findByShortCode(String shortCode);

    boolean existsByShortCode(String shortCode);

    int incrementAccessCount(Long id);

    int deleteByShortCode(String shortCode);
}
