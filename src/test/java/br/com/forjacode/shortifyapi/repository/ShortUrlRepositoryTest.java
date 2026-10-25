package br.com.forjacode.shortifyapi.repository;

import br.com.forjacode.shortifyapi.model.ShortUrl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ShortUrlRepositoryTest {

    @Container
    @ServiceConnection
    static final MySQLContainer MYSQL = new MySQLContainer("mysql:8.4");

    private static final String CODE = "abc1234";
    private static final String URL = "https://example.com";

    @Autowired
    private ShortUrlRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    private ShortUrl persist(String code) {
        ShortUrl shortUrl = entityManager.persistAndFlush(new ShortUrl(URL, code));
        entityManager.clear();
        return shortUrl;
    }

    @Nested
    @DisplayName("FindByShortCode Tests")
    class FindByShortCode {

        @Nested
        @DisplayName("Should find ShortUrl successfully")
        class Success {

            @Test
            void shouldReturnShortUrlWhenCodeExists() {
                persist(CODE);

                assertThat(repository.findByShortCode(CODE))
                        .hasValueSatisfying(found -> {
                            assertThat(found.getShortCode()).isEqualTo(CODE);
                            assertThat(found.getOriginalUrl()).isEqualTo(URL);
                        });
            }
        }

        @Nested
        @DisplayName("Should return empty when code does not exist")
        class WithError {

            @Test
            void shouldReturnEmptyWhenCodeDoesNotExist() {
                assertThat(repository.findByShortCode(CODE)).isEmpty();
            }
        }
    }

    @Nested
    @DisplayName("ExistsByShortCode Tests")
    class ExistsByShortCode {

        @Nested
        @DisplayName("Should check existence successfully")
        class Success {

            @Test
            void shouldReturnTrueWhenCodeExists() {
                persist(CODE);

                assertThat(repository.existsByShortCode(CODE)).isTrue();
            }
        }

        @Nested
        @DisplayName("Should return false when code does not exist")
        class WithError {

            @Test
            void shouldReturnFalseWhenCodeDoesNotExist() {
                assertThat(repository.existsByShortCode(CODE)).isFalse();
            }
        }
    }

    @Nested
    @DisplayName("FindAccessCountByShortCode Tests")
    class FindAccessCountByShortCode {

        @Nested
        @DisplayName("Should return access count successfully")
        class Success {

            @Test
            void shouldReturnZeroForNewShortUrl() {
                persist(CODE);

                assertThat(repository.findAccessCountByShortCode(CODE)).contains(0L);
            }

            @Test
            void shouldReturnUpdatedCountAfterIncrement() {
                ShortUrl saved = persist(CODE);
                repository.incrementAccessCount(saved.getId());
                repository.incrementAccessCount(saved.getId());

                assertThat(repository.findAccessCountByShortCode(CODE)).contains(2L);
            }
        }

        @Nested
        @DisplayName("Should return empty when code does not exist")
        class WithError {

            @Test
            void shouldReturnEmptyWhenCodeDoesNotExist() {
                assertThat(repository.findAccessCountByShortCode(CODE)).isEmpty();
            }
        }
    }

    @Nested
    @DisplayName("IncrementAccessCount Tests")
    class IncrementAccessCount {

        @Nested
        @DisplayName("Should increment access count successfully")
        class Success {

            @Test
            void shouldIncrementAndReturnAffectedRows() {
                ShortUrl saved = persist(CODE);

                int affected = repository.incrementAccessCount(saved.getId());

                assertThat(affected).isEqualTo(1);
                assertThat(repository.findByShortCode(CODE).orElseThrow().getAccessCount()).isEqualTo(1L);
            }
        }

        @Nested
        @DisplayName("Should affect no rows when id does not exist")
        class WithError {

            @Test
            void shouldReturnZeroWhenIdDoesNotExist() {
                assertThat(repository.incrementAccessCount(999L)).isZero();
            }
        }
    }

    @Nested
    @DisplayName("DeleteByShortCode Tests")
    class DeleteByShortCode {

        @Nested
        @DisplayName("Should delete ShortUrl successfully")
        class Success {

            @Test
            void shouldDeleteAndReturnAffectedRows() {
                persist(CODE);
                persist("other01");

                int affected = repository.deleteByShortCode(CODE);

                assertThat(affected).isEqualTo(1);
                assertThat(repository.existsByShortCode(CODE)).isFalse();
                assertThat(repository.existsByShortCode("other01")).isTrue();
            }
        }

        @Nested
        @DisplayName("Should affect no rows when code does not exist")
        class WithError {

            @Test
            void shouldReturnZeroWhenCodeDoesNotExist() {
                assertThat(repository.deleteByShortCode(CODE)).isZero();
            }
        }
    }
}
