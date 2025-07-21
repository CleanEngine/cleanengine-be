package com.cleanengine.coin.order.application.dto;

import com.cleanengine.coin.order.domain.BuyOrder;
import com.cleanengine.coin.order.domain.Order;
import com.cleanengine.coin.order.domain.OrderStatus;

import static com.cleanengine.coin.common.CommonValues.approxEquals;

public record OrderCancelResult(
    String ticker,
    Long orderId,
    ExecutionStatus executionStatus,
    String side,
    String orderType,
    Double price,
    Double remainingSize
) {
    public OrderCancelResult(Order order){
        this(order.getTicker(), order.getId(), getExecutionStatus(order), getSide(order), getOrderType(order), getPrice(order), getRemainingSize(order));
    }

    private static String getSide(Order order) {
        return order instanceof BuyOrder ? "bid" : "ask";
    }

    private static String getOrderType(Order order) {
        return order.getIsMarketOrder()? "market" : "limit";
    }

    private static ExecutionStatus getExecutionStatus(Order order) {
        if(order.getState() == OrderStatus.DONE) return ExecutionStatus.ALL_EXECUTED;
        if(order instanceof BuyOrder buyOrder) {
            if(approxEquals(buyOrder.getRemainingDeposit(), buyOrder.getLockedDeposit())) {
                return ExecutionStatus.NONE;
            }
            else {
                return ExecutionStatus.PARTIAL_EXECUTED;
            }
        }
        else {
            if(approxEquals(order.getRemainingSize(), order.getOrderSize())) {
                return ExecutionStatus.NONE;
            }
            else {
                return ExecutionStatus.PARTIAL_EXECUTED;
            }
        }
    }

    private static Double getPrice(Order order) {
        return order.getIsMarketOrder() ? null : order.getPrice();
    }

    private static Double getRemainingSize(Order order) {
        if(order instanceof BuyOrder && order.getIsMarketOrder()) {
            return null;
        }
        else {
            return order.getRemainingSize();
        }
    }

    public enum ExecutionStatus {
        ALL_EXECUTED, PARTIAL_EXECUTED, NONE
    }
}
