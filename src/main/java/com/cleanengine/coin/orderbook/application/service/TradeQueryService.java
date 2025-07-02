package com.cleanengine.coin.orderbook.application.service;

import com.cleanengine.coin.orderbook.dto.ClosingPriceDto;
import com.cleanengine.coin.orderbook.infra.TradeQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TradeQueryService {
    private final TradeQueryRepository tradeQueryRepository;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ClosingPriceDto getYesterdayClosingPrice(String ticker, LocalDate yesterdayDate) {
        return tradeQueryRepository.getYesterdayClosingPrice(ticker, yesterdayDate);
    }
}
