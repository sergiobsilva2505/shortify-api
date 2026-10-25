package br.com.forjacode.shortifyapi.adapter.in.web;

import br.com.forjacode.shortifyapi.domain.exception.ShortUrlNotFoundException;
import br.com.forjacode.shortifyapi.adapter.in.web.mapper.ShortUrlMapper;
import br.com.forjacode.shortifyapi.application.port.in.ShortUrlUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ShortUrlController.class)
class ShortUrlControllerDeleteTest {

    private static final String API = "/api/shorten";
    private static final String CODE = "abc1234";
    private static final String UNKNOWN_CODE = "zzz9999";
    private static final String MALFORMED_CODE = "abc";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ShortUrlUseCase service;

    @MockitoBean
    private ShortUrlMapper mapper;

    @Nested
    @DisplayName("Delete ShortUrl Tests")
    class Delete {

        @Nested
        @DisplayName("Should delete ShortUrl successfully")
        class Success {

            @Test
            void shouldReturnNoContentForExistingCode() throws Exception {
                mockMvc.perform(delete(API + "/" + CODE))
                        .andExpect(status().isNoContent());

                verify(service).delete(CODE);
            }
        }

        @Nested
        @DisplayName("Should return error when code is unknown or malformed")
        class WithError {

            @Test
            void shouldReturnNotFoundForUnknownCode() throws Exception {
                doThrow(new ShortUrlNotFoundException(UNKNOWN_CODE)).when(service).delete(UNKNOWN_CODE);

                mockMvc.perform(delete(API + "/" + UNKNOWN_CODE))
                        .andExpect(status().isNotFound());
            }

            @Test
            void shouldRejectCodeOutsideExpectedFormat() throws Exception {
                mockMvc.perform(delete(API + "/" + MALFORMED_CODE))
                        .andExpect(status().isNotFound());

                verifyNoInteractions(service);
            }
        }
    }

    @Nested
    @DisplayName("Find ShortUrl Tests")
    class FindByCode {

        @Nested
        @DisplayName("Should return error when code is malformed")
        class WithError {

            @Test
            void shouldRejectCodeOutsideExpectedFormat() throws Exception {
                mockMvc.perform(get(API + "/" + MALFORMED_CODE))
                        .andExpect(status().isNotFound());

                verifyNoInteractions(service);
            }
        }
    }

    @Nested
    @DisplayName("Update ShortUrl Tests")
    class Update {

        @Nested
        @DisplayName("Should return error when code is malformed")
        class WithError {

            @Test
            void shouldRejectCodeOutsideExpectedFormat() throws Exception {
                mockMvc.perform(put(API + "/" + MALFORMED_CODE)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"url\":\"https://example.com\"}"))
                        .andExpect(status().isNotFound());

                verifyNoInteractions(service);
            }
        }
    }
}
