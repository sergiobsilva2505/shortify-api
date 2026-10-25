package br.com.forjacode.shortifyapi;

import br.com.forjacode.shortifyapi.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ShortifyApiApplicationTests extends IntegrationTestSupport {

    @Nested
    @DisplayName("Application Context Tests")
    class ContextLoads {

        @Nested
        @DisplayName("Should start application successfully")
        class Success {

            @Test
            void shouldLoadContextWithLiquibaseSchemaValidated() {
            }
        }
    }

    @Nested
    @DisplayName("Main Tests")
    class Main {

        @Nested
        @DisplayName("Should start application successfully")
        class Success {

            @Test
            void shouldRunMainMethod() {
                ShortifyApiApplication.main(new String[]{
                        "--spring.main.web-application-type=none",
                        "--spring.datasource.url=" + MYSQL.getJdbcUrl(),
                        "--spring.datasource.username=" + MYSQL.getUsername(),
                        "--spring.datasource.password=" + MYSQL.getPassword()
                });
            }
        }
    }
}
