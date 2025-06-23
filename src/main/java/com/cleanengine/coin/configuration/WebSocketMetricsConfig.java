package com.cleanengine.coin.configuration;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
class WebSocketMetricsInterceptor implements ChannelInterceptor {

    private final Counter inboundMessages;
    private final Counter outboundMessages;
    private final Counter connectionCounter;
    private final Counter disconnectionCounter;
    private final Counter subscriptionCounter;
    private final Counter unsubscriptionCounter;
    private final Counter errorCounter;
    private final Timer messageProcessingTime;
    private final AtomicInteger activeConnections = new AtomicInteger(0);

    public WebSocketMetricsInterceptor(MeterRegistry meterRegistry) {
        this.inboundMessages = Counter.builder("stomp_messages_inbound_total")
                .description("Total inbound STOMP messages")
                .register(meterRegistry);

        this.outboundMessages = Counter.builder("stomp_messages_outbound_total")
                .description("Total outbound STOMP messages")
                .register(meterRegistry);

        this.connectionCounter = Counter.builder("stomp_connections_total")
                .description("Total STOMP connections")
                .register(meterRegistry);

        this.disconnectionCounter = Counter.builder("stomp_disconnections_total")
                .description("Total STOMP disconnections")
                .register(meterRegistry);

        this.subscriptionCounter = Counter.builder("stomp_subscriptions_total")
                .description("Total STOMP subscriptions")
                .register(meterRegistry);

        this.unsubscriptionCounter = Counter.builder("stomp_unsubscriptions_total")
                .description("Total STOMP unsubscriptions")
                .register(meterRegistry);

        this.errorCounter = Counter.builder("stomp_errors_total")
                .description("Total STOMP errors")
                .register(meterRegistry);

        this.messageProcessingTime = Timer.builder("stomp_message_processing_duration")
                .description("STOMP message processing time")
                .register(meterRegistry);

        // Active connections gauge
        Gauge.builder("stomp_active_connections", activeConnections, AtomicInteger::get)
                .description("Current active STOMP connections")
                .register(meterRegistry);    // 2) register(registry) 만 호출
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        StompCommand command = accessor.getCommand();

        if (command != null) {
            String ticker = extractTicker(accessor);

            switch (command) {
                case CONNECT:
                    connectionCounter.increment();
                    activeConnections.incrementAndGet();
                    log.debug("STOMP CONNECT - Active connections: {}", activeConnections.get());
                    break;

                case DISCONNECT:
                    disconnectionCounter.increment();
                    activeConnections.decrementAndGet();
                    log.debug("STOMP DISCONNECT - Active connections: {}", activeConnections.get());
                    break;

                case SUBSCRIBE:
                    subscriptionCounter.increment();
                    log.debug("STOMP SUBSCRIBE - Destination: {}, Ticker: {}",
                            accessor.getDestination(), ticker);
                    break;

                case UNSUBSCRIBE:
                    unsubscriptionCounter.increment();
                    log.debug("STOMP UNSUBSCRIBE - Ticker: {}", ticker);
                    break;

                case SEND:
                    inboundMessages.increment();
                    log.debug("STOMP SEND - Destination: {}, Ticker: {}",
                            accessor.getDestination(), ticker);
                    break;

                case MESSAGE:
                    outboundMessages.increment();
                    log.debug("STOMP MESSAGE - Destination: {}, Ticker: {}",
                            accessor.getDestination(), ticker);
                    break;

                case ERROR:
                    errorCounter.increment();
                    log.error("STOMP ERROR - Ticker: {}", ticker);
                    break;
            }
        }

        return message;
    }

    @Override
    public void postSend(Message<?> message, MessageChannel channel, boolean sent) {
        if (!sent) {
            errorCounter.increment();
            log.error("Message failed to send: {}", message);
        }
    }

    private String extractTicker(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        if (destination != null) {
            // /topic/realTimeTradeRate/BTC -> BTC
            // /app/subscribe/realTimeTradeRate/BTC -> BTC
            String[] parts = destination.split("/");
            if (parts.length > 0) {
                return parts[parts.length - 1];
            }
        }
        return "unknown";
    }
}