package br.com.forjacode.shortifyapi.mapper;

import br.com.forjacode.shortifyapi.dto.response.ShortUrlResponse;
import br.com.forjacode.shortifyapi.model.ShortUrl;
import org.springframework.stereotype.Component;

@Component
public class ShortUrlMapper {

    public ShortUrlResponse toResponse(ShortUrl shortUrl, String baseUrl) {
        return new ShortUrlResponse(
                shortUrl.getId(),
                shortUrl.getOriginalUrl(),
                shortUrl.getShortCode(),
                buildShortUrl(baseUrl, shortUrl.getShortCode()),
                shortUrl.getCreatedAt(),
                shortUrl.getUpdatedAt()
        );
    }

    private String buildShortUrl(String baseUrl, String shortCode) {
        String normalizedBaseUrl = baseUrl.endsWith("/")
                ? baseUrl.substring(0, baseUrl.length() - 1)
                : baseUrl;
        return normalizedBaseUrl + "/" + shortCode;
    }
}

