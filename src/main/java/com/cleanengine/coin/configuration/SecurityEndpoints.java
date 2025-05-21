package com.cleanengine.coin.configuration;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.List;

@Configuration
public class SecurityEndpoints {

    @Value("${spring.security.endpoints.websocket.paths}")
    private List<String> websocketEndpoints;

    @Value("${spring.security.endpoints.public.paths}")
    private List<String> publicPaths;

    @Bean
    public EndpointConfig endpointConfig() {
        return new EndpointConfig(publicPaths, websocketEndpoints);
    }

    @Data
    @AllArgsConstructor
    public static class EndpointConfig {
        private final List<String> publicPaths;
        private final List<String> websocketEndpoints;

        public boolean isPublicPath(String path) {
            return publicPaths.stream()
                    .anyMatch(path::startsWith);
        }

        public String[] getPublicPathPatterns() {
            return publicPaths.stream()
                    .map(path -> path + "/**")
                    .toArray(String[]::new);
        }

        public List<String> getWebsocketEndpoints() {
            return Collections.unmodifiableList(websocketEndpoints);
        }
    }
}
