package br.com.forjacode.shortifyapi.mapper;

import br.com.forjacode.shortifyapi.dto.response.ShortUrlResponse;
import br.com.forjacode.shortifyapi.dto.response.StatsResponse;
import br.com.forjacode.shortifyapi.model.ShortUrl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShortUrlMapperTest {

    private static final String CODE = "abc1234";
    private static final String URL = "https://example.com";
    private static final String BASE_URL = "http://localhost:8080";

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

                ShortUrlResponse response = mapper.toResponse(shortUrl, BASE_URL);

                assertThat(response.id()).isEqualTo(1L);
                assertThat(response.url()).isEqualTo(URL);
                assertThat(response.shortCode()).isEqualTo(CODE);
                assertThat(response.shortUrl()).isEqualTo(BASE_URL + "/" + CODE);
                assertThat(response.createdAt()).isEqualTo(shortUrl.getCreatedAt());
                assertThat(response.updatedAt()).isEqualTo(shortUrl.getUpdatedAt());
            }

            @Test
            void shouldNotDuplicateSlashWhenBaseUrlEndsWithSlash() {
                ShortUrlResponse response = mapper.toResponse(newShortUrl(), BASE_URL + "/");

                assertThat(response.shortUrl()).isEqualTo(BASE_URL + "/" + CODE);
            }

            @Test
            void shouldSupportEmptyBaseUrl() {
                ShortUrlResponse response = mapper.toResponse(newShortUrl(), "");

                assertThat(response.shortUrl()).isEqualTo("/" + CODE);
            }
        }

        @Nested
        @DisplayName("Should throw exception for null input")
        class WithError {

            @Test
            void shouldThrowExceptionWhenShortUrlIsNull() {
                assertThatThrownBy(() -> mapper.toResponse(null, BASE_URL))
                        .isInstanceOf(NullPointerException.class);
            }

            @Test
            void shouldThrowExceptionWhenBaseUrlIsNull() {
                ShortUrl shortUrl = newShortUrl();

                assertThatThrownBy(() -> mapper.toResponse(shortUrl, null))
                        .isInstanceOf(NullPointerException.class);
            }
        }
    }

    @Nested
    @DisplayName("ToStatsResponse Tests")
    class ToStatsResponse {

        @Nested
        @DisplayName("Should map stats successfully")
        class Success {

            @Test
            void shouldMapCodeAndAccessCount() {
                StatsResponse response = mapper.toStatsResponse(CODE, 5L);

                assertThat(response.code()).isEqualTo(CODE);
                assertThat(response.accessCount()).isEqualTo(5L);
            }

            @Test
            void shouldMapZeroAccessCount() {
                assertThat(mapper.toStatsResponse(CODE, 0L).accessCount()).isZero();
            }
        }
    }
}
