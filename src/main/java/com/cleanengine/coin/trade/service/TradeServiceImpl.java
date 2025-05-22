package com.cleanengine.coin.trade.service;

import com.cleanengine.coin.trade.entity.Trade;
import com.cleanengine.coin.trade.repository.TradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class TradeServiceImpl implements TradeService {

    private final TradeRepository tradeRepository;
    private final Random random = new Random();

    @Override
    public Trade createTrade(String ticker, Double price, Double size, Integer buyUserId, Integer sellUserId) {
        Trade trade = new Trade();
        trade.setTicker(ticker);
        trade.setPrice(price);
        trade.setSize(size);
        trade.setBuyUserId(buyUserId);
        trade.setSellUserId(sellUserId);
        trade.setTradeTime(LocalDateTime.now());
        return tradeRepository.save(trade);
    }

    @Override
    public Trade generateRandomTrade() {
        // 임의의 티커 목록
        String[] tickers = {"BTC", "TRUMP"};
        String ticker = tickers[random.nextInt(tickers.length)];

        // 가격: 1000 ~ 50000 (소수점 2자리로 반올림)
        double price = Math.round((1000 + (50000 - 1000) * random.nextDouble()) * 100) / 100.0;

        // 거래량: 0.1 ~ 5.0 (소수점 2자리로 반올림)
        double size = Math.round((0.1 + (5.0 - 0.1) * random.nextDouble()) * 100) / 100.0;

        // 임의의 사용자 ID 생성
        int buyUserId = random.nextInt(1000) + 1;  // 1 ~ 1000
        int sellUserId = random.nextInt(1000) + 1;

        // 동일한 사용자 간 거래 방지
        while (buyUserId == sellUserId) {
            sellUserId = random.nextInt(1000) + 1;
        }

        return createTrade(ticker, price, size, buyUserId, sellUserId);
    }
}