package br.com.forjacode.shortifyapi.application.service;

import br.com.forjacode.shortifyapi.domain.exception.InvalidUrlException;
import br.com.forjacode.shortifyapi.domain.exception.ShortUrlNotFoundException;
import br.com.forjacode.shortifyapi.domain.exception.ShortCodeAlreadyExistsException;
import br.com.forjacode.shortifyapi.domain.model.ShortUrl;
import br.com.forjacode.shortifyapi.application.port.out.ShortUrlRepositoryPort;
import br.com.forjacode.shortifyapi.application.port.out.ShortCodeGeneratorPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShortUrlServiceTest {

    private static final String CODE = "abc1234";
    private static final String URL = "https://example.com";

    @Mock
    private ShortUrlRepositoryPort repository;

    @Mock
    private ShortCodeGeneratorPort codeGenerator;

    @InjectMocks
    private ShortUrlService service;

    private ShortUrl newShortUrl() {
        ShortUrl shortUrl = new ShortUrl(URL, CODE);
        ReflectionTestUtils.setField(shortUrl, "id", 1L);
        return shortUrl;
    }

    @Nested
    @DisplayName("Create ShortUrl Tests")
    class Create {

        @Nested
        @DisplayName("Should create ShortUrl successfully")
        class Success {

            @Test
            void shouldCreateShortUrl() {
                when(codeGenerator.generate()).thenReturn(CODE);
                when(repository.existsByShortCode(CODE)).thenReturn(false);
                when(repository.save(any(ShortUrl.class))).thenAnswer(inv -> inv.getArgument(0));

                ShortUrl result = service.create(URL);

                assertThat(result.getOriginalUrl()).isEqualTo(URL);
                assertThat(result.getShortCode()).isEqualTo(CODE);
                assertThat(result.getAccessCount()).isZero();
                verify(repository).save(any(ShortUrl.class));
            }

            @Test
            void shouldRetryWithNewCodeWhenSaveViolatesUniqueConstraint() {
                when(codeGenerator.generate()).thenReturn("taken01", CODE);
                when(repository.existsByShortCode(any())).thenReturn(false);
                when(repository.save(any(ShortUrl.class)))
                        .thenThrow(new ShortCodeAlreadyExistsException("duplicate"))
                        .thenAnswer(inv -> inv.getArgument(0));

                ShortUrl result = service.create(URL);

                assertThat(result.getShortCode()).isEqualTo(CODE);
                verify(repository, times(2)).save(any(ShortUrl.class));
            }

            @Test
            void shouldAcceptHttpScheme() {
                when(codeGenerator.generate()).thenReturn(CODE);
                when(repository.existsByShortCode(CODE)).thenReturn(false);
                when(repository.save(any(ShortUrl.class))).thenAnswer(inv -> inv.getArgument(0));

                ShortUrl result = service.create("http://example.com");

                assertThat(result.getOriginalUrl()).isEqualTo("http://example.com");
            }

            @Test
            void shouldRegenerateCodeWhenItAlreadyExists() {
                when(codeGenerator.generate()).thenReturn("taken01", CODE);
                when(repository.existsByShortCode("taken01")).thenReturn(true);
                when(repository.existsByShortCode(CODE)).thenReturn(false);
                when(repository.save(any(ShortUrl.class))).thenAnswer(inv -> inv.getArgument(0));

                ShortUrl result = service.create(URL);

                assertThat(result.getShortCode()).isEqualTo(CODE);
                verify(codeGenerator, times(2)).generate();
            }
        }

        @Nested
        @DisplayName("Should throw exception for invalid URL")
        class WithError {

            @ParameterizedTest
            @ValueSource(strings = {"not a url", "example.com", "ftp://example.com", "http://", "https:///path"})
            void shouldThrowExceptionForInvalidUrl(String invalidUrl) {
                assertThatThrownBy(() -> service.create(invalidUrl))
                        .isInstanceOf(InvalidUrlException.class);

                verifyNoInteractions(codeGenerator);
                verify(repository, never()).save(any());
            }

            @Test
            void shouldGiveUpAfterRepeatedUniqueConstraintViolations() {
                when(codeGenerator.generate()).thenReturn(CODE);
                when(repository.existsByShortCode(CODE)).thenReturn(false);
                when(repository.save(any(ShortUrl.class))).thenThrow(new ShortCodeAlreadyExistsException("duplicate"));

                assertThatThrownBy(() -> service.create(URL))
                        .isInstanceOf(ShortCodeAlreadyExistsException.class);

                verify(repository, times(5)).save(any(ShortUrl.class));
            }
        }
    }

    @Nested
    @DisplayName("FindByShortCode Tests")
    class FindByShortCode {

        @Nested
        @DisplayName("Should find ShortUrl successfully")
        class Success {

            @Test
            void shouldReturnShortUrlWhenCodeExists() {
                ShortUrl shortUrl = newShortUrl();
                when(repository.findByShortCode(CODE)).thenReturn(Optional.of(shortUrl));

                assertThat(service.findByShortCode(CODE)).isSameAs(shortUrl);
            }
        }

        @Nested
        @DisplayName("Should throw exception when code does not exist")
        class WithError {

            @Test
            void shouldThrowNotFoundWhenCodeDoesNotExist() {
                when(repository.findByShortCode(CODE)).thenReturn(Optional.empty());

                assertThatThrownBy(() -> service.findByShortCode(CODE))
                        .isInstanceOf(ShortUrlNotFoundException.class);
            }
        }
    }

    @Nested
    @DisplayName("ResolveAndRegisterAccess Tests")
    class ResolveAndRegisterAccess {

        @Nested
        @DisplayName("Should resolve and register access successfully")
        class Success {

            @Test
            void shouldReturnShortUrlAndIncrementAccessCount() {
                ShortUrl shortUrl = newShortUrl();
                when(repository.findByShortCode(CODE)).thenReturn(Optional.of(shortUrl));
                when(repository.incrementAccessCount(1L)).thenReturn(1);

                ShortUrl result = service.resolveAndRegisterAccess(CODE);

                assertThat(result).isSameAs(shortUrl);
                verify(repository).incrementAccessCount(1L);
            }
        }

        @Nested
        @DisplayName("Should throw exception when ShortUrl is not found")
        class WithError {

            @Test
            void shouldThrowNotFoundWhenCodeDoesNotExist() {
                when(repository.findByShortCode(CODE)).thenReturn(Optional.empty());

                assertThatThrownBy(() -> service.resolveAndRegisterAccess(CODE))
                        .isInstanceOf(ShortUrlNotFoundException.class);

                verify(repository, never()).incrementAccessCount(any());
            }

            @Test
            void shouldThrowNotFoundWhenRowIsDeletedBeforeIncrement() {
                ShortUrl shortUrl = newShortUrl();
                when(repository.findByShortCode(CODE)).thenReturn(Optional.of(shortUrl));
                when(repository.incrementAccessCount(1L)).thenReturn(0);

                assertThatThrownBy(() -> service.resolveAndRegisterAccess(CODE))
                        .isInstanceOf(ShortUrlNotFoundException.class);
            }
        }
    }

    @Nested
    @DisplayName("Update ShortUrl Tests")
    class Update {

        private static final String NEW_URL = "https://example.org/new";

        @Nested
        @DisplayName("Should update ShortUrl successfully")
        class Success {

            @Test
            void shouldUpdateOriginalUrl() {
                ShortUrl shortUrl = newShortUrl();
                when(repository.findByShortCode(CODE)).thenReturn(Optional.of(shortUrl));
                when(repository.save(shortUrl)).thenAnswer(inv -> inv.getArgument(0));

                ShortUrl result = service.update(CODE, NEW_URL);

                assertThat(result).isSameAs(shortUrl);
                assertThat(result.getOriginalUrl()).isEqualTo(NEW_URL);
                assertThat(result.getShortCode()).isEqualTo(CODE);
                verify(repository).save(shortUrl);
            }
        }

        @Nested
        @DisplayName("Should throw exception on invalid input")
        class WithError {

            @Test
            void shouldThrowNotFoundWhenCodeDoesNotExist() {
                when(repository.findByShortCode(CODE)).thenReturn(Optional.empty());

                assertThatThrownBy(() -> service.update(CODE, NEW_URL))
                        .isInstanceOf(ShortUrlNotFoundException.class);
            }

            @ParameterizedTest
            @ValueSource(strings = {"not a url", "example.com", "ftp://example.com", "http://"})
            void shouldThrowExceptionForInvalidUrlAndKeepOriginal(String invalidUrl) {
                ShortUrl shortUrl = newShortUrl();
                when(repository.findByShortCode(CODE)).thenReturn(Optional.of(shortUrl));

                assertThatThrownBy(() -> service.update(CODE, invalidUrl))
                        .isInstanceOf(InvalidUrlException.class);

                assertThat(shortUrl.getOriginalUrl()).isEqualTo(URL);
            }
        }
    }

    @Nested
    @DisplayName("Delete ShortUrl Tests")
    class Delete {

        @Nested
        @DisplayName("Should delete ShortUrl successfully")
        class Success {

            @Test
            void shouldRemoveExistingCode() {
                when(repository.deleteByShortCode(CODE)).thenReturn(1);

                assertThatCode(() -> service.delete(CODE)).doesNotThrowAnyException();

                verify(repository).deleteByShortCode(CODE);
            }
        }

        @Nested
        @DisplayName("Should throw exception when code does not exist")
        class WithError {

            @Test
            void shouldThrowNotFoundWhenCodeDoesNotExist() {
                when(repository.deleteByShortCode(CODE)).thenReturn(0);

                assertThatThrownBy(() -> service.delete(CODE))
                        .isInstanceOf(ShortUrlNotFoundException.class);
            }
        }
    }

    @Nested
    @DisplayName("GetAccessCount Tests")
    class GetAccessCount {

        @Nested
        @DisplayName("Should return access count successfully")
        class Success {

            @Test
            void shouldReturnAccessCount() {
                when(repository.findAccessCountByShortCode(CODE)).thenReturn(Optional.of(5L));

                assertThat(service.getAccessCount(CODE)).isEqualTo(5L);
            }
        }

        @Nested
        @DisplayName("Should throw exception when code does not exist")
        class WithError {

            @Test
            void shouldThrowNotFoundWhenCodeDoesNotExist() {
                when(repository.findAccessCountByShortCode(CODE)).thenReturn(Optional.empty());

                assertThatThrownBy(() -> service.getAccessCount(CODE))
                        .isInstanceOf(ShortUrlNotFoundException.class);
            }
        }
    }
}
