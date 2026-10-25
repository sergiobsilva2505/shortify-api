package br.com.forjacode.shortifyapi.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI shortifyOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Shortify API")
                .description("API RESTful de encurtamento de URLs, com CRUD completo e estatísticas de acesso.")
                .version("v1")
                .contact(new Contact().name("ForjaCode")));
    }
}
