package com.cleanengine.coin.orderbook.infra;

import com.cleanengine.coin.base.WebSocketTest;
import com.cleanengine.coin.orderbook.dto.OrderBookInfo;
import com.cleanengine.coin.orderbook.dto.OrderBookUnitInfo;
import com.cleanengine.coin.tool.helper.GenericStompFrameHandler;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Disabled
public class OrderBookUpdatedNotifierAdapterTest extends WebSocketTest {

    @Autowired
    protected OrderBookUpdatedNotifierAdapter orderBookUpdatedNotifierAdapter;

    @Test
    public void getOrderBooks() throws Exception {
        OrderBookInfo orderBookInfo = new OrderBookInfo("BTC",
                List.of(new OrderBookUnitInfo(1.0, 1.0)),
                List.of(new OrderBookUnitInfo( 2.0, 2.0)));

        session.subscribe("/topic/orderbook/BTC",
                new GenericStompFrameHandler<>(OrderBookInfo.class, responseQueue));

        orderBookUpdatedNotifierAdapter.sendOrderBooks(orderBookInfo);

        OrderBookInfo result = (OrderBookInfo) responseQueue.poll(10, TimeUnit.SECONDS);

        assertEquals(orderBookInfo, result);
    }
}
