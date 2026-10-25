package br.com.forjacode.shortifyapi.config;

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
class ActuatorEndpointsTest extends IntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Nested
    @DisplayName("Actuator Endpoints Tests (/actuator)")
    class Endpoints {

        @Nested
        @DisplayName("Should expose operational endpoints successfully")
        class Success {

            @Test
            void shouldReportHealthUp() throws Exception {
                mockMvc.perform(get("/actuator/health"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.status").value("UP"));
            }

            @Test
            void shouldExposeLivenessAndReadinessProbes() throws Exception {
                mockMvc.perform(get("/actuator/health/liveness"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.status").value("UP"));
                mockMvc.perform(get("/actuator/health/readiness"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.status").value("UP"));
            }

            @Test
            void shouldExposeInfo() throws Exception {
                mockMvc.perform(get("/actuator/info"))
                        .andExpect(status().isOk());
            }
        }

        @Nested
        @DisplayName("Should not expose sensitive endpoints")
        class WithError {

            @Test
            void shouldNotExposeEnv() throws Exception {
                mockMvc.perform(get("/actuator/env"))
                        .andExpect(status().isNotFound());
            }
        }
    }
}
