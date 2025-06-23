package com.cleanengine.coin.configuration;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${spring.security.allowed-origins}")
    private String[] allowedOrigins;

    public WebSocketConfig(WebSocketMetricsInterceptor webSocketMetricsInterceptor) {
        this.webSocketMetricsInterceptor = webSocketMetricsInterceptor;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic"); //메세지 브로커로 라우팅 되어야한다
        config.setApplicationDestinationPrefixes("/app"); //app으로 시작되는 메세지가 message-handling 라우팅 되어야한다
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registerEndpoint(registry, "/api/coin/min");
        registerEndpoint(registry, "/api/coin/orderbook");
        registerEndpoint(registry, "/api/coin/prev");
    }

    private void registerEndpoint(StompEndpointRegistry registry, String endpoint) {
        registry.addEndpoint(endpoint)
                .setAllowedOrigins(allowedOrigins);
    }

    private final WebSocketMetricsInterceptor webSocketMetricsInterceptor;

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(webSocketMetricsInterceptor);
    }

    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        registration.interceptors(webSocketMetricsInterceptor);
    }

}

