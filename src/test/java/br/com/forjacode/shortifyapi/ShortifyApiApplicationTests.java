package br.com.forjacode.shortifyapi;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;

@Testcontainers
@SpringBootTest
class ShortifyApiApplicationTests {

    @Container
    @ServiceConnection
    static final MySQLContainer MYSQL = new MySQLContainer("mysql:8.4");

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
