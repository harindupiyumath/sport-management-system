package com.wayamba.sportms.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    // Comma-separated list of deployed frontend origins (e.g. the Vercel URL)
    @Value("${app.cors.allowed-origin:}")
    private String origins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        List<String> patterns = new ArrayList<>(List.of("http://localhost:[*]", "http://127.0.0.1:[*]"));
        for (String o : origins.split(",")) {
            if (!o.isBlank()) {
                patterns.add(o.trim().replaceAll("/+$", "")); // tolerate a trailing slash
            }
        }

        registry.addMapping("/**")
                // Any local port (Live Server may pick 5500, 5501, ...) plus the configured origins
                .allowedOriginPatterns(patterns.toArray(String[]::new))
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}

