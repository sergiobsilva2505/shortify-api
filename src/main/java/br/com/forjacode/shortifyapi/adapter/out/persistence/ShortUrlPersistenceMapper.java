package br.com.forjacode.shortifyapi.adapter.out.persistence;

import br.com.forjacode.shortifyapi.domain.model.ShortUrl;
import org.springframework.stereotype.Component;

@Component
public class ShortUrlPersistenceMapper {

    public ShortUrl toDomain(ShortUrlJpaEntity entity) {
        return ShortUrl.restore(
                entity.getId(),
                entity.getOriginalUrl(),
                entity.getShortCode(),
                entity.getAccessCount(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public ShortUrlJpaEntity toEntity(ShortUrl shortUrl) {
        return new ShortUrlJpaEntity(
                shortUrl.getId(),
                shortUrl.getOriginalUrl(),
                shortUrl.getShortCode(),
                shortUrl.getAccessCount(),
                shortUrl.getCreatedAt(),
                shortUrl.getUpdatedAt()
        );
    }
}
