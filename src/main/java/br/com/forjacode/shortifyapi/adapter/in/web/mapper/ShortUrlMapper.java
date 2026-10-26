package br.com.forjacode.shortifyapi.adapter.in.web.mapper;

import br.com.forjacode.shortifyapi.adapter.in.web.dto.response.ShortUrlResponse;
import br.com.forjacode.shortifyapi.adapter.in.web.dto.response.ShortUrlStatsResponse;
import br.com.forjacode.shortifyapi.domain.model.ShortUrl;
import org.springframework.stereotype.Component;

@Component
public class ShortUrlMapper {

    public ShortUrlResponse toResponse(ShortUrl shortUrl) {
        return new ShortUrlResponse(
                shortUrl.getId(),
                shortUrl.getOriginalUrl(),
                shortUrl.getShortCode(),
                shortUrl.getCreatedAt(),
                shortUrl.getUpdatedAt()
        );
    }

    public ShortUrlStatsResponse toStatsResponse(ShortUrl shortUrl) {
        return new ShortUrlStatsResponse(
                shortUrl.getId(),
                shortUrl.getOriginalUrl(),
                shortUrl.getShortCode(),
                shortUrl.getCreatedAt(),
                shortUrl.getUpdatedAt(),
                shortUrl.getAccessCount()
        );
    }
}

