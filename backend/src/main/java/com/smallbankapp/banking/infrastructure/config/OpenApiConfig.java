package com.smallbankapp.banking.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Small Banking App API")
                        .description("""
                                Online Banking System — Technical Interview Challenge.
                                
                                ## Authentication
                                Use `POST /api/auth/login` to obtain a JWT token, then click **Authorize** 
                                and enter `Bearer <token>` to authenticate all secured endpoints.
                                
                                ## Test Credentials (dev profile)
                                - `ihernandez@email.com` / `Isabel2024!`
                                - `mjimenez@example.com` / `Miguel2024!`
                                - `paulamolina@mail.com` / `Paula2024!`
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Small Banking App")
                                .url("https://github.com/johnmikepty/small-baking-app")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .name("bearerAuth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Enter your JWT token. Example: Bearer eyJhbGci...")));
    }
}
