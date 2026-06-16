package com.medibook.config.swagger;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

        private static final String SECURITY_SCHEME_NAME = "Bearer";

        @Bean
        public OpenAPI customOpenAPI() {

                return new OpenAPI()
                                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                                .components(new Components().addSecuritySchemes(SECURITY_SCHEME_NAME,
                                                new SecurityScheme().name(SECURITY_SCHEME_NAME)
                                                                .type(SecurityScheme.Type.HTTP)
                                                                .scheme("bearer").bearerFormat("JWT")))
                                .info(
                                                new Info().title("MediBook API").version("v1.0.0")
                                                                .description("Online Medical Appointment Booking System")
                                                                .contact(new Contact().name("MediBook Team")
                                                                                .email("support@medibook.com"))
                                                                .license(new License().name("Internal Use")));
        }
}
