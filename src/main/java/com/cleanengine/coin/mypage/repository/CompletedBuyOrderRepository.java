package com.cleanengine.coin.mypage.repository;

import com.cleanengine.coin.order.domain.BuyOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompletedBuyOrderRepository extends JpaRepository<BuyOrder, Long> {
    List<BuyOrder> findAllByUserIdOrderByCreatedAtDesc(Integer userId);
    List<BuyOrder> findAllByUserIdAndIsBotFalse(Integer userId);
}
