package com.cleanengine.coin.realitybot.dto;

import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Ticks {
    private String market;
    private String trade_date_utc; // LocalDate
    private String trade_time_utc; // LocalTime
    private String timestamp; //instant 에러 발생
    private float trade_price;
    private double trade_volume;
    private float prev_closing_price;
    private double change_price;
    private String ask_bid;
    private long sequential_id;

    @Override
    public String toString() {
        return "Ticks{" +
                "market='" + market + '\'' +
                ", trade_date_utc='" + trade_date_utc + '\'' +
                ", trade_time_utc='" + trade_time_utc + '\'' +
                ", timestamp=" + timestamp +
                ", trade_price=" + trade_price +
                ", trade_volume=" + trade_volume +
                ", prev_closing_price=" + prev_closing_price +
                ", change_price=" + change_price +
                ", ask_bid='" + ask_bid + '\'' +
                ", sequential_id=" + sequential_id +
                '}';
    }
}
