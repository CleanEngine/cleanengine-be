package com.cleanengine.coin.mypage.repository;

import com.cleanengine.coin.mypage.dto.CompletedOrderDto;
import com.cleanengine.coin.trade.entity.Trade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompletedOrderRepository extends JpaRepository<Trade,Integer> {
    /**
     * 주문 완료 목록 조회
     **/
    List<Trade> findAllByBuyUserIdOrderByTradeTimeAsc(Integer buyUserId);
    Trade findFirstByTickerOrderByTradeTimeDesc(String ticker);

 }
