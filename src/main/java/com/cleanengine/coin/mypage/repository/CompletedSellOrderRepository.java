package com.cleanengine.coin.mypage.repository;

import com.cleanengine.coin.order.domain.SellOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompletedSellOrderRepository extends JpaRepository<SellOrder, Integer> {
    List<SellOrder> findAllByUserIdOrderByCreatedAtDesc(Integer userId);
    List<SellOrder> findAllByUserIdAndIsBotFalse(Integer userId);
}
