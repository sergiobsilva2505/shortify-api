package br.com.forjacode.shortifyapi.dto.response;

import java.time.Instant;

public record ShortUrlResponse(Long id, String url, String shortCode, String shortUrl, Instant createdAt, Instant updatedAt) {
}
