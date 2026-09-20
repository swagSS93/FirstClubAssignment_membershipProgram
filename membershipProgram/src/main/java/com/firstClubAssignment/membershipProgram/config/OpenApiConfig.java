package com.firstClubAssignment.membershipProgram.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Membership Program API")
                        .version("1.0.0")
                        .description("REST API documentation for managing membership plans, dynamic tier benefits, subscriptions, and order activity.")
                        .contact(new Contact()
                                .name("Engineering Team")
                                .email("swagatasinha16@yahoo.in")));
    }
}
