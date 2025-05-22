package com.cleanengine.coin.trade.service;

import com.cleanengine.coin.trade.entity.Trade;

public interface TradeService {

    /**
     * 거래 데이터를 생성하여 저장합니다.
     *
     * @param ticker 종목 코드
     * @param price 가격
     * @param size 거래량
     * @return 저장된 Trade 엔티티
     */
    Trade createTrade(String ticker, Double price, Double size, Integer buyUserId, Integer sellUserId);

    /**
     * 임의의 거래 데이터를 생성하여 저장합니다.
     * 테스트/개발용 메서드입니다.
     *
     * @return 저장된 Trade 엔티티
     */
    Trade generateRandomTrade();
}