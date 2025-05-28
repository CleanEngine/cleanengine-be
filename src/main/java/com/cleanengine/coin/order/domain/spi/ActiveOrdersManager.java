package com.cleanengine.coin.order.domain.spi;

import java.io.Closeable;

public interface ActiveOrdersManager extends Closeable {

    ActiveOrders getActiveOrders(String ticker);

    void removeActiveOrders(String ticker);

    @Override
    void close();
}
