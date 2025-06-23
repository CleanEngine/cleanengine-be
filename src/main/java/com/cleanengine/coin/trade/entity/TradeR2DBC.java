package com.cleanengine.coin.trade.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("trade") // (1) 테이블 매핑
public class TradeR2DBC {

    @Id // (2) 기본 키 매핑
    @Column("trade_id")
    private Integer id;

    @Column("ticker") // (3) 컬럼 매핑
    private String ticker;

    @Column("trade_time")
    private LocalDateTime tradeTime;

    @Column("buy_user_id")
    private Integer buyUserId;

    @Column("sell_user_id")
    private Integer sellUserId;

    @Column("price")
    private Double price;

    @Column("size")
    private Double size;

}
