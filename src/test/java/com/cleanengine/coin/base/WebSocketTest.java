package com.cleanengine.coin.base;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"dev", "it", "h2-mem"})
public abstract class WebSocketTest {

    @LocalServerPort
    protected int port;

    protected WebSocketStompClient stompClient;
    protected StompSession session;

    protected BlockingQueue<Object> responseQueue = new LinkedBlockingQueue<>();

    @BeforeEach
    public void setUp() throws Exception {
        responseQueue.clear();
        stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        String wsUrl = "ws://localhost:" + port + "/api/coin/min";
        CompletableFuture<StompSession> connectFuture = stompClient.connectAsync(wsUrl, new StompSessionHandlerAdapter() {
            @Override
            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                System.out.println("Test STOMP Client Connected: " + session.getSessionId());
            }
            @Override
            public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
                System.err.println("STOMP Exception: " + exception);
                exception.printStackTrace();
            }
            @Override
            public void handleTransportError(StompSession session, Throwable exception) {
                System.err.println("Transport error: " + exception);
                exception.printStackTrace();
            }
        });

        session = connectFuture.get(5, TimeUnit.SECONDS);
    }
}
