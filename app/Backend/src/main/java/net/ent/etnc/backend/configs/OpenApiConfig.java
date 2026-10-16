package net.ent.etnc.backend.configs;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().addSecurityItem(
                new SecurityRequirement().addList("bearerAuth")
        ).components(
                new Components().addSecuritySchemes("bearerAuth", createAPIKeyScheme())
        ).info(
                new Info().title("Certification Center API")
                        .description("API REST pour la gestion des certifications et inscriptions")
                        .version("1.0.0")
        );
    }

    private SecurityScheme createAPIKeyScheme() {
        return new SecurityScheme().type(SecurityScheme.Type.HTTP)
                .bearerFormat("JWT")
                .scheme("bearer");
    }
}
