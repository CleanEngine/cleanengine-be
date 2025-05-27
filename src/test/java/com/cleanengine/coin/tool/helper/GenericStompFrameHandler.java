package com.cleanengine.coin.tool.helper;

import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;

import java.lang.reflect.Type;
import java.util.concurrent.BlockingQueue;

public class GenericStompFrameHandler<T> implements StompFrameHandler {
    private final Class<T> payloadType;
    private final BlockingQueue<Object> outputQueue;

    public GenericStompFrameHandler(Class<T> payloadType, BlockingQueue<Object> queue) {
        this.payloadType = payloadType;
        this.outputQueue = queue;
    }

    @Override
    public Type getPayloadType(StompHeaders headers) {
        return payloadType;
    }

    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
        if(payload == null){
            return;
        }

        if(!payloadType.isInstance(payload)){
            throw new RuntimeException("Unexpected payload type: " + payload.getClass());
        }
        outputQueue.add(payload);
    }
}
