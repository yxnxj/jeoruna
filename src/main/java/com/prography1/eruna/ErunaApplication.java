package com.prography1.eruna;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@OpenAPIDefinition(servers = {@Server(url = "https://eruna.site",description = "Eruna Server"),
		@Server(url = "http://localhost:8080",description = "Local server")})
@EnableJpaAuditing
@SpringBootApplication
public class ErunaApplication {

	public static void main(String[] args) {
		SpringApplication.run(ErunaApplication.class, args);
	}

}
