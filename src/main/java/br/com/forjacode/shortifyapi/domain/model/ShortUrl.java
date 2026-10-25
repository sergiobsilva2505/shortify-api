package br.com.forjacode.shortifyapi.domain.model;

import java.time.Instant;

public class ShortUrl {

    private Long id;
    private String originalUrl;
    private String shortCode;
    private Long accessCount;
    private Instant createdAt;
    private Instant updatedAt;

    public ShortUrl(String originalUrl, String shortCode) {
        this.originalUrl = originalUrl;
        this.shortCode = shortCode;
        this.accessCount = 0L;
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    private ShortUrl() {
    }

    public static ShortUrl restore(Long id, String originalUrl, String shortCode, Long accessCount,
                                   Instant createdAt, Instant updatedAt) {
        ShortUrl shortUrl = new ShortUrl();
        shortUrl.id = id;
        shortUrl.originalUrl = originalUrl;
        shortUrl.shortCode = shortCode;
        shortUrl.accessCount = accessCount;
        shortUrl.createdAt = createdAt;
        shortUrl.updatedAt = updatedAt;
        return shortUrl;
    }

    public void updateOriginalUrl(String newOriginalUrl) {
        this.originalUrl = newOriginalUrl;
        this.updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public String getShortCode() {
        return shortCode;
    }

    public Long getAccessCount() {
        return accessCount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
