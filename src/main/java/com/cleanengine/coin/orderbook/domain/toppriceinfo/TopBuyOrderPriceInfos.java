package com.cleanengine.coin.orderbook.domain.toppriceinfo;

import com.cleanengine.coin.orderbook.domain.BuyOrderPriceInfo;
import lombok.Synchronized;

import java.util.concurrent.ConcurrentSkipListSet;

public class TopBuyOrderPriceInfos extends TopOrderPriceInfos<BuyOrderPriceInfo> {
    public TopBuyOrderPriceInfos(int topCount) {
        super(topCount);
    }

    @Synchronized
    @Override
    public void addNewOrderPriceInfo(BuyOrderPriceInfo OrderPriceInfo) {
        if(topOrderPriceInfos.size() == topCount) {
            BuyOrderPriceInfo mostExpensivePriceInfoInTop = (BuyOrderPriceInfo) topOrderPriceInfos.last();
            if(OrderPriceInfo.compareTo(mostExpensivePriceInfoInTop) > 0) {
                topOrderPriceInfos.pollLast();
            }
        }
        topOrderPriceInfos.add(OrderPriceInfo);
    }

    @Synchronized
    @Override
    public void renewOrderPriceInfo(ConcurrentSkipListSet<BuyOrderPriceInfo> buyOrderPriceInfoListSet) {
        // 10개 불러오기
    }
}
