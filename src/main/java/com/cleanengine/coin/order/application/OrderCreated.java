package com.cleanengine.coin.order.application;

import com.cleanengine.coin.order.domain.Order;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderCreated {
    private final Order order;
    public OrderCreated(Order order){
        this.order = order;
    }
}
