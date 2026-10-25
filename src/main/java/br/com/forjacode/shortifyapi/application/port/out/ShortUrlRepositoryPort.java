package br.com.forjacode.shortifyapi.application.port.out;

import br.com.forjacode.shortifyapi.domain.model.ShortUrl;

import java.util.Optional;

public interface ShortUrlRepositoryPort {

    /**
     * Persiste a short URL. Para uma nova, lança ShortCodeAlreadyExistsException se o código já existir;
     * para uma existente, atualiza apenas a URL original e a data de atualização.
     */
    ShortUrl save(ShortUrl shortUrl);

    Optional<ShortUrl> findByShortCode(String shortCode);

    boolean existsByShortCode(String shortCode);

    Optional<Long> findAccessCountByShortCode(String shortCode);

    int incrementAccessCount(Long id);

    int deleteByShortCode(String shortCode);
}
