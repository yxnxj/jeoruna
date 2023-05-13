package com.prography1.eruna.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String API_NAME = "ERUNA API";
    private static final String API_VERSION = "1.0.0";
    private static final String API_DESCRIPTION = "ERUNA API 명세서";


    @Bean
    public OpenAPI openAPI() {
        Info info = new Info()
                .version(API_VERSION)
                .title(API_NAME)
                .description(API_DESCRIPTION);

        return new OpenAPI()
                .info(info);
    }
}