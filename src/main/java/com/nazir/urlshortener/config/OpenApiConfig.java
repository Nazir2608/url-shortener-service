package com.nazir.urlshortener.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${app.base-url}")
    private String baseUrl;

    @Bean
    public OpenAPI urlShortenerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("URL Shortener API")
                        .description("""
                                A high-performance URL shortening service with:
                                - Custom & auto-generated slugs
                                - Click tracking & analytics
                                - Geo-location & device detection
                                - Rate limiting by tier
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Nazir")
                                .url("https://github.com/nazir"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server().url(baseUrl).description("Current environment")))
                .externalDocs(new ExternalDocumentation()
                        .description("Project README")
                        .url("https://github.com/nazir/url-shortener-service"));
    }
}
