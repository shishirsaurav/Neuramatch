package com.neuramatch.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("NeuraMatch API")
                        .version("1.0.0")
                        .description("AI-Powered Semantic Resume and Job Matching Engine\n\n" +
                                "NeuraMatch uses advanced NLP, vector search, and knowledge graphs " +
                                "to match candidates with jobs based on skills, experience, and career goals.")
                        .contact(new Contact()
                                .name("NeuraMatch Team")
                                .email("support@neuramatch.io")
                                .url("https://neuramatch.io"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://neuramatch.io/license")))
                .servers(Arrays.asList(
                        new Server()
                                .url("http://localhost:8081")
                                .description("Resume Service (Local)"),
                        new Server()
                                .url("http://localhost:8082")
                                .description("Job Service (Local)"),
                        new Server()
                                .url("http://localhost:8083")
                                .description("Matching Service (Local)"),
                        new Server()
                                .url("https://api.neuramatch.io")
                                .description("Production API")
                ));
    }
}
