package br.com.forjacode.shortifyapi.controller;

import br.com.forjacode.shortifyapi.exception.ShortUrlNotFoundException;
import br.com.forjacode.shortifyapi.mapper.ShortUrlMapper;
import br.com.forjacode.shortifyapi.service.ShortUrlService;
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

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ShortUrlService service;

    @MockitoBean
    private ShortUrlMapper mapper;

    @Test
    void deleteReturnsNoContentForExistingCode() throws Exception {
        mockMvc.perform(delete("/api/shorten/abc1234"))
                .andExpect(status().isNoContent());

        verify(service).delete("abc1234");
    }

    @Test
    void deleteReturnsNotFoundForUnknownCode() throws Exception {
        doThrow(new ShortUrlNotFoundException("zzz9999")).when(service).delete("zzz9999");

        mockMvc.perform(delete("/api/shorten/zzz9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteRejectsCodeOutsideExpectedFormat() throws Exception {
        mockMvc.perform(delete("/api/shorten/abc"))
                .andExpect(status().isNotFound());

        verifyNoInteractions(service);
    }

    @Test
    void getRejectsCodeOutsideExpectedFormat() throws Exception {
        mockMvc.perform(get("/api/shorten/abc"))
                .andExpect(status().isNotFound());

        verifyNoInteractions(service);
    }

    @Test
    void putRejectsCodeOutsideExpectedFormat() throws Exception {
        mockMvc.perform(put("/api/shorten/abc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"https://example.com\"}"))
                .andExpect(status().isNotFound());

        verifyNoInteractions(service);
    }
}
