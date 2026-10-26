package br.com.forjacode.shortifyapi.adapter.out.persistence;

import br.com.forjacode.shortifyapi.support.IntegrationTestSupport;
import br.com.forjacode.shortifyapi.domain.exception.ShortCodeAlreadyExistsException;
import br.com.forjacode.shortifyapi.domain.model.ShortUrl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import({ShortUrlPersistenceAdapter.class, ShortUrlPersistenceMapper.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ShortUrlPersistenceAdapterTest extends IntegrationTestSupport {

    private static final String CODE = "abc1234";
    private static final String URL = "https://example.com";

    @Autowired
    private ShortUrlPersistenceAdapter repository;

    @Autowired
    private TestEntityManager entityManager;

    // O MySQL é compartilhado com as demais classes de integração (e reaproveitado entre execuções);
    // a limpeza roda dentro da transação do teste, que é revertida ao final.
    @BeforeEach
    void cleanTable() {
        entityManager.getEntityManager().createQuery("delete from ShortUrlJpaEntity").executeUpdate();
    }

    private static ShortUrlJpaEntity newEntity(String code) {
        Instant now = Instant.now();
        return new ShortUrlJpaEntity(null, URL, code, 0L, now, now);
    }

    private ShortUrlJpaEntity persist(String code) {
        ShortUrlJpaEntity shortUrl = entityManager.persistAndFlush(newEntity(code));
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
    @DisplayName("IncrementAccessCount Tests")
    class IncrementAccessCount {

        @Nested
        @DisplayName("Should increment access count successfully")
        class Success {

            @Test
            void shouldIncrementAndReturnAffectedRows() {
                ShortUrlJpaEntity saved = persist(CODE);

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

    @Nested
    @DisplayName("Save Tests")
    class Save {

        @Nested
        @DisplayName("Should save ShortUrl successfully")
        class Success {

            @Test
            void shouldInsertNewShortUrlAndAssignId() {
                ShortUrl saved = repository.save(new ShortUrl(URL, CODE));

                assertThat(saved.getId()).isNotNull();
                assertThat(saved.getShortCode()).isEqualTo(CODE);
                assertThat(saved.getAccessCount()).isZero();
            }

            @Test
            void shouldUpdateOnlyUrlAndUpdatedAtOfExistingShortUrl() {
                ShortUrlJpaEntity persisted = persist(CODE);
                repository.incrementAccessCount(persisted.getId());
                ShortUrl staleDomain = ShortUrl.restore(persisted.getId(), URL, CODE, 0L,
                        persisted.getCreatedAt(), persisted.getUpdatedAt());
                staleDomain.updateOriginalUrl("https://example.org/new");

                ShortUrl updated = repository.save(staleDomain);

                assertThat(updated.getOriginalUrl()).isEqualTo("https://example.org/new");
            }
        }

        @Nested
        @DisplayName("Should translate unique constraint violation")
        class WithError {

            @Test
            void shouldThrowShortCodeAlreadyExistsWhenCodeIsTaken() {
                persist(CODE);

                assertThatThrownBy(() -> repository.save(new ShortUrl(URL, CODE)))
                        .isInstanceOf(ShortCodeAlreadyExistsException.class);
            }
        }
    }
}
