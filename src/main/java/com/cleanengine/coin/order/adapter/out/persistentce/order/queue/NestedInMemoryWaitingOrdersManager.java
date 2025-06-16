package com.cleanengine.coin.order.adapter.out.persistentce.order.queue;

import com.cleanengine.coin.order.domain.spi.WaitingOrders;
import com.cleanengine.coin.order.domain.spi.WaitingOrdersManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Optional;

@Slf4j
//@Component
public class NestedInMemoryWaitingOrdersManager implements WaitingOrdersManager {
    private final HashMap<String, WaitingOrders> waitingOrdersMap = new HashMap<>();

    @Override
    public WaitingOrders getWaitingOrders(String ticker) {
        if(!waitingOrdersMap.containsKey(ticker)) {
            addWaitingOrders(ticker);
        }

        Optional<WaitingOrders> waitingOrdersOpt = Optional.ofNullable(waitingOrdersMap.get(ticker));
        if(waitingOrdersOpt.isEmpty()){
            log.debug("WaitingOrders not found. with " + ticker);
            throw new RuntimeException("WaitingOrders not found with " + ticker);
        }
        return waitingOrdersOpt.get();    }

    @Override
    public void removeWaitingOrders(String ticker) {
        waitingOrdersMap.remove(ticker);
    }

    @Override
    public void close() {

    }

    protected synchronized void addWaitingOrders(String ticker){
        if(!waitingOrdersMap.containsKey(ticker)){
            waitingOrdersMap.put(ticker, new NestedInMemoryWaitingOrders(ticker));
        }
    }
}
