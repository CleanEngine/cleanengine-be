package com.cleanengine.coin.orderbook.dto;

import com.cleanengine.coin.order.domain.Order;

public record OrderCanceled (
        Order order
){ }
