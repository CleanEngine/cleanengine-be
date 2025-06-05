package com.cleanengine.coin.order.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "asset")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Asset {
    @Id @Column(name = "ticker", length = 10, nullable = false)
    private String ticker;
    @Column(name = "name", length = 100)
    private String name;
    @Column(name = "icon", columnDefinition = "BLOB") @Lob @Setter
    private byte[] icon;

    public Asset(String ticker, String name){
        if(ticker == null || name == null) throw new IllegalArgumentException("ticker, name cannot be null");
        this.ticker = ticker;
        this.name = name;
    }

    public Asset(String ticker, String name, byte[] icon){
        this(ticker, name);
        this.icon = icon;
    }
}
