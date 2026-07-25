package com.gestionlocal.gestion.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig
{
    @Bean
    public OpenAPI customOpenAPI()
    {
        return new OpenAPI()
                .servers(List.of(
                        new Server().url("https://gestion-production-989a.up.railway.app").description("Producción"),
                        new Server().url("http://localhost:8080").description("Local")
                ));
    }
}