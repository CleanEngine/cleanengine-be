package com.cleanengine.coin.order.domain.spi;

import java.io.Closeable;

public interface WaitingOrdersManager extends Closeable {
    WaitingOrders getWaitingOrders(String ticker);

    void removeWaitingOrders(String ticker);

    @Override
    void close();
}
