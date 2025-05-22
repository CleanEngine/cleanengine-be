package com.cleanengine.coin.realitybot.dto;

import lombok.*;

@AllArgsConstructor
@Data
@Builder
@Getter
@Setter
public class TestOrder {
    public enum Type{BUY,SELL}
    private final Type type;
    private double price;
    private double volume;
    private long timestamp;


    @Override
    public String toString() {
        return "TestOrder{" +
                "type=" + type +
                ", price=" + price +
                ", volum=" + volume +
                ", timestamp=" + timestamp +
                '}';
    }
}
