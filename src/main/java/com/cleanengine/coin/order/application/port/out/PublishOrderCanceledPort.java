package com.cleanengine.coin.order.application.port.out;

import com.cleanengine.coin.orderbook.dto.OrderCanceled;

public interface PublishOrderCanceledPort {
    void publish(OrderCanceled orderCanceled);
}
