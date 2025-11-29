package com.sonnhuynhh.shopnest.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

/**
 * EnvironmentPostProcessor to load environment variables from .env file
 * This runs before Spring Boot processes application.properties
 * This allows keeping secrets out of version control
 */
public class DotenvConfig implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        try {
            Dotenv dotenv = Dotenv.configure()
                    .ignoreIfMissing()
                    .load();

            Map<String, Object> envMap = new HashMap<>();
            dotenv.entries().forEach(entry -> {
                envMap.put(entry.getKey(), entry.getValue());
            });

            environment.getPropertySources().addFirst(new MapPropertySource("dotenvProperties", envMap));
        } catch (Exception e) {
            // .env file not found - using system environment variables
            System.out.println("Note: .env file not found, using system environment variables");
        }
    }
}
