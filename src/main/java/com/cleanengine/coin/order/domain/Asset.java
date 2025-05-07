package com.cleanengine.coin.order.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "asset")
@NoArgsConstructor(access = AccessLevel.PROTECTED) @AllArgsConstructor
@Getter
public class Asset {
    @Id @Column(name = "ticker", length = 10, nullable = false)
    private String ticker;
    @Column(name = "name", length = 100)
    private String name;
}
