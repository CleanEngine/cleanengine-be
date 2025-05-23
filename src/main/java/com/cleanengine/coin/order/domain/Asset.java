package com.cleanengine.coin.order.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "asset")
@NoArgsConstructor(access = AccessLevel.PROTECTED) @AllArgsConstructor
@Getter
public class Asset {
    @Id @Column(name = "ticker", length = 10, nullable = false)
    private String ticker;
    @Column(name = "name", length = 100)
    private String name;
    @Column(name = "icon") @Lob @Setter
    private byte[] icon;

    public Asset(String ticker, String name){
        this.ticker = ticker;
        this.name = name;
    }
}
