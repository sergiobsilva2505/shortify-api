package br.com.forjacode.shortifyapi.service.impl;

import br.com.forjacode.shortifyapi.exception.ShortUrlNotFoundException;
import br.com.forjacode.shortifyapi.model.ShortUrl;
import br.com.forjacode.shortifyapi.repository.ShortUrlRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShortUrlServiceImplTest {

    @Mock
    private ShortUrlRepository repository;

    @InjectMocks
    private ShortUrlServiceImpl service;

    @Test
    void deleteRemovesExistingCode() {
        when(repository.deleteByShortCode("abc1234")).thenReturn(1);

        assertThatCode(() -> service.delete("abc1234")).doesNotThrowAnyException();
    }

    @Test
    void deleteThrowsNotFoundWhenCodeDoesNotExist() {
        when(repository.deleteByShortCode("abc1234")).thenReturn(0);

        assertThatThrownBy(() -> service.delete("abc1234"))
                .isInstanceOf(ShortUrlNotFoundException.class);
    }

    @Test
    void resolveThrowsNotFoundWhenRowIsDeletedBeforeIncrement() {
        ShortUrl shortUrl = new ShortUrl("https://example.com", "abc1234");
        when(repository.findByShortCode("abc1234")).thenReturn(Optional.of(shortUrl));
        when(repository.incrementAccessCount(shortUrl.getId())).thenReturn(0);

        assertThatThrownBy(() -> service.resolveAndRegisterAccess("abc1234"))
                .isInstanceOf(ShortUrlNotFoundException.class);
    }
}
