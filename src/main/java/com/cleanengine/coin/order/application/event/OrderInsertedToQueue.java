package com.cleanengine.coin.order.application.event;

import com.cleanengine.coin.order.domain.Order;

public record OrderInsertedToQueue (
    Order order
){};
