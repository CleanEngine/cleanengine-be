package com.cleanengine.coin.configuration;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${spring.security.allowed-origins}")
    private String[] allowedOrigins;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic"); //메세지 브로커로 라우팅 되어야한다
        config.setApplicationDestinationPrefixes("/app"); //app으로 시작되는 메세지가 message-handling 라우팅 되어야한다
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registerEndpoint(registry, "/api/coin/min");
        registerEndpoint(registry, "/api/coin/orderbook");
    }

    private void registerEndpoint(StompEndpointRegistry registry, String endpoint) {
        registry.addEndpoint(endpoint)
                .setAllowedOrigins(allowedOrigins);
    }

}