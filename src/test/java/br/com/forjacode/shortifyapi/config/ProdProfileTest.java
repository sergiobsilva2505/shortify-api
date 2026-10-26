package br.com.forjacode.shortifyapi.config;

import br.com.forjacode.shortifyapi.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("prod")
class ProdProfileTest extends IntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Nested
    @DisplayName("Prod Profile Tests")
    class Prod {

        @Nested
        @DisplayName("Should keep operational endpoints available")
        class Success {

            @Test
            void shouldKeepHealthAvailable() throws Exception {
                mockMvc.perform(get("/actuator/health"))
                        .andExpect(status().isOk());
            }
        }

        @Nested
        @DisplayName("Should hide documentation and metrics")
        class WithError {

            @Test
            void shouldDisableOpenApiDocs() throws Exception {
                mockMvc.perform(get("/v3/api-docs"))
                        .andExpect(status().isNotFound());
            }

            @Test
            void shouldDisableSwaggerUi() throws Exception {
                mockMvc.perform(get("/swagger-ui/index.html"))
                        .andExpect(status().isNotFound());
            }

            @Test
            void shouldNotExposeMetrics() throws Exception {
                mockMvc.perform(get("/actuator/metrics"))
                        .andExpect(status().isNotFound());
            }
        }
    }
}
