package com.cleanengine.coin.orderbook.domain.toppriceinfo;

import com.cleanengine.coin.orderbook.domain.SellOrderPriceInfo;

import java.util.concurrent.ConcurrentSkipListSet;

public class TopSellOrderPriceInfos extends TopOrderPriceInfos<SellOrderPriceInfo> {
    public TopSellOrderPriceInfos(int topCount) {
        super(topCount);
    }

    @Override
    public void addNewOrderPriceInfo(SellOrderPriceInfo OrderPriceInfo) {
        if(topOrderPriceInfos.size() == topCount) {
            SellOrderPriceInfo mostCheapPriceInfoInTop = (SellOrderPriceInfo) topOrderPriceInfos.last();
            if(OrderPriceInfo.compareTo(mostCheapPriceInfoInTop) > 0) {
                topOrderPriceInfos.pollLast();
            }
        }
        topOrderPriceInfos.add(OrderPriceInfo);
    }

    @Override
    public void renewOrderPriceInfo(ConcurrentSkipListSet<SellOrderPriceInfo> topOrderPriceInfos) {

    }
}
