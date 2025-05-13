package com.cleanengine.coin.orderbook.domain.toppriceinfo;

import com.cleanengine.coin.orderbook.domain.OrderPriceInfo;

import java.util.concurrent.ConcurrentSkipListSet;

public abstract class TopOrderPriceInfos<T extends OrderPriceInfo> {
    // 작은 결과가 first로, 큰 결과가 last로
    protected final ConcurrentSkipListSet<T> topOrderPriceInfos = new ConcurrentSkipListSet<>();
    protected final int topCount;

    public TopOrderPriceInfos(int topCount) {
        this.topCount = topCount;
    }
    public abstract void addNewOrderPriceInfo(T OrderPriceInfo);

    public abstract void renewOrderPriceInfo(ConcurrentSkipListSet<T> topOrderPriceInfos);
}
