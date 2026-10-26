package br.com.forjacode.shortifyapi.adapter.in.web;

import br.com.forjacode.shortifyapi.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OpenApiDocsTest extends IntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Nested
    @DisplayName("OpenAPI Docs Tests (GET /v3/api-docs)")
    class ApiDocs {

        @Nested
        @DisplayName("Should expose the API documentation successfully")
        class Success {

            @Test
            void shouldDescribeApiAndShortenEndpoints() throws Exception {
                mockMvc.perform(get("/v3/api-docs"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.info.title").value("Shortify API"))
                        .andExpect(jsonPath("$.paths['/shorten'].post").exists())
                        .andExpect(jsonPath("$.paths['/shorten/{code}'].get").exists())
                        .andExpect(jsonPath("$.paths['/shorten/{code}'].put").exists())
                        .andExpect(jsonPath("$.paths['/shorten/{code}'].delete").exists())
                        .andExpect(jsonPath("$.paths['/shorten/{code}/stats'].get").exists());
            }

            @Test
            void shouldDocumentOperationsDeclaredInApiInterface() throws Exception {
                mockMvc.perform(get("/v3/api-docs"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.paths['/shorten'].post.tags[0]").value("Short URLs"))
                        .andExpect(jsonPath("$.paths['/shorten'].post.summary").value("Cria uma URL curta"))
                        .andExpect(jsonPath("$.paths['/shorten'].post.responses['201']").exists())
                        .andExpect(jsonPath("$.paths['/shorten'].post.responses['400']").exists())
                        .andExpect(jsonPath("$.paths['/shorten/{code}'].put.responses['404']").exists())
                        .andExpect(jsonPath("$.paths['/shorten/{code}'].delete.responses['204']").exists())
                        .andExpect(jsonPath("$.paths['/shorten/{code}'].get.parameters[0].name").value("code"))
                        .andExpect(jsonPath("$.paths['/shorten/{code}'].get.parameters[0].description")
                                .value(ShortUrlApi.CODE_DESCRIPTION))
                        .andExpect(jsonPath("$.paths['/shorten/{code}'].get.parameters[0].example")
                                .value(ShortUrlApi.CODE_EXAMPLE));
            }

            @Test
            void shouldServeSwaggerUi() throws Exception {
                mockMvc.perform(get("/swagger-ui/index.html"))
                        .andExpect(status().isOk());
            }
        }
    }
}
