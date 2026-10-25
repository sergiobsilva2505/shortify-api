package br.com.forjacode.shortifyapi.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class ShortUrlTest {

    private static final String CODE = "abc1234";
    private static final String URL = "https://example.com";
    private static final String NEW_URL = "https://example.org/new";

    @Nested
    @DisplayName("Constructor Tests")
    class Constructor {

        @Nested
        @DisplayName("Should create ShortUrl successfully")
        class Success {

            @Test
            void shouldInitializeFields() {
                Instant before = Instant.now();

                ShortUrl shortUrl = new ShortUrl(URL, CODE);

                assertThat(shortUrl.getId()).isNull();
                assertThat(shortUrl.getOriginalUrl()).isEqualTo(URL);
                assertThat(shortUrl.getShortCode()).isEqualTo(CODE);
                assertThat(shortUrl.getAccessCount()).isZero();
                assertThat(shortUrl.getCreatedAt()).isBetween(before, Instant.now());
                assertThat(shortUrl.getUpdatedAt()).isEqualTo(shortUrl.getCreatedAt());
            }
        }
    }

    @Nested
    @DisplayName("UpdateOriginalUrl Tests")
    class UpdateOriginalUrl {

        @Nested
        @DisplayName("Should update URL successfully")
        class Success {

            @Test
            void shouldChangeUrlAndRefreshUpdatedAtOnly() {
                ShortUrl shortUrl = new ShortUrl(URL, CODE);
                Instant createdAt = shortUrl.getCreatedAt();

                shortUrl.updateOriginalUrl(NEW_URL);

                assertThat(shortUrl.getOriginalUrl()).isEqualTo(NEW_URL);
                assertThat(shortUrl.getShortCode()).isEqualTo(CODE);
                assertThat(shortUrl.getCreatedAt()).isEqualTo(createdAt);
                assertThat(shortUrl.getUpdatedAt()).isAfterOrEqualTo(createdAt);
            }
        }
    }
}
