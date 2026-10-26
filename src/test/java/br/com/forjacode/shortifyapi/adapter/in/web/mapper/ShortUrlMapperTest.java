package br.com.forjacode.shortifyapi.adapter.in.web.mapper;

import br.com.forjacode.shortifyapi.adapter.in.web.dto.response.ShortUrlResponse;
import br.com.forjacode.shortifyapi.adapter.in.web.dto.response.ShortUrlStatsResponse;
import br.com.forjacode.shortifyapi.domain.model.ShortUrl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShortUrlMapperTest {

    private static final String CODE = "abc1234";
    private static final String URL = "https://example.com";

    private final ShortUrlMapper mapper = new ShortUrlMapper();

    private ShortUrl newShortUrl() {
        ShortUrl shortUrl = new ShortUrl(URL, CODE);
        ReflectionTestUtils.setField(shortUrl, "id", 1L);
        return shortUrl;
    }

    @Nested
    @DisplayName("ToResponse Tests")
    class ToResponse {

        @Nested
        @DisplayName("Should map ShortUrl to response successfully")
        class Success {

            @Test
            void shouldMapAllFields() {
                ShortUrl shortUrl = newShortUrl();

                ShortUrlResponse response = mapper.toResponse(shortUrl);

                assertThat(response.id()).isEqualTo(1L);
                assertThat(response.url()).isEqualTo(URL);
                assertThat(response.shortCode()).isEqualTo(CODE);
                assertThat(response.createdAt()).isEqualTo(shortUrl.getCreatedAt());
                assertThat(response.updatedAt()).isEqualTo(shortUrl.getUpdatedAt());
            }
        }

        @Nested
        @DisplayName("Should throw exception for null input")
        class WithError {

            @Test
            void shouldThrowExceptionWhenShortUrlIsNull() {
                assertThatThrownBy(() -> mapper.toResponse(null))
                        .isInstanceOf(NullPointerException.class);
            }
        }
    }

    @Nested
    @DisplayName("ToStatsResponse Tests")
    class ToShortUrlStatsResponse {

        @Nested
        @DisplayName("Should map stats successfully")
        class Success {

            @Test
            void shouldMapAllFieldsIncludingAccessCount() {
                ShortUrl shortUrl = newShortUrl();
                ReflectionTestUtils.setField(shortUrl, "accessCount", 5L);

                ShortUrlStatsResponse response = mapper.toStatsResponse(shortUrl);

                assertThat(response.id()).isEqualTo(1L);
                assertThat(response.url()).isEqualTo(URL);
                assertThat(response.shortCode()).isEqualTo(CODE);
                assertThat(response.createdAt()).isEqualTo(shortUrl.getCreatedAt());
                assertThat(response.updatedAt()).isEqualTo(shortUrl.getUpdatedAt());
                assertThat(response.accessCount()).isEqualTo(5L);
            }

            @Test
            void shouldMapZeroAccessCount() {
                ShortUrl shortUrl = newShortUrl();

                assertThat(mapper.toStatsResponse(shortUrl).accessCount()).isZero();
            }
        }

        @Nested
        @DisplayName("Should throw exception for null input")
        class WithError {

            @Test
            void shouldThrowExceptionWhenShortUrlIsNull() {
                assertThatThrownBy(() -> mapper.toStatsResponse(null))
                        .isInstanceOf(NullPointerException.class);
            }
        }
    }
}
