package br.com.forjacode.shortifyapi.service;

import br.com.forjacode.shortifyapi.model.ShortUrl;

public interface ShortUrlService {

    ShortUrl create(String originalUrl);

    ShortUrl findByShortCode(String shortCode);
}
