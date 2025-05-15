package com.cleanengine.coin.chart.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic"); //메세지 브로커로 라우팅 되어야한다
        config.setApplicationDestinationPrefixes("/app"); //app으로 시작되는 메세지가 message-handling 라우팅 되어야한다
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry
                .addEndpoint("/coin/min") //endPoint 지정
                //추후 cors url추가
    .setAllowedOrigins("http://localhost:63342", "http://localhost:8080", "http://localhost:5500", "http://localhost:5173");
        registry
                .addEndpoint("/coin/realtime")
                .setAllowedOrigins("http://localhost:63342", "http://localhost:8080", "http://localhost:5500", "http://localhost:5173");
        registry
                .addEndpoint("/coin/orderbook")
                .setAllowedOrigins("http://localhost:63342", "http://localhost:8080", "http://localhost:5500", "http://localhost:5173");
    }
}
