package com.kossentini.portfolio.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI portfolioOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Portfolio Risk Management API")
                        .description("REST API for managing investment portfolios, instruments, transactions, and risk/valuation metrics")
                        .version("v0.1.0")
                        .contact(new Contact().name("Mohamed Kossentini")));
    }
}
