package com.cleanengine.coin.order.integration.sellorder;

import com.cleanengine.coin.common.error.DomainValidationException;
import com.cleanengine.coin.order.application.OrderCommand;
import com.cleanengine.coin.order.application.OrderInfo;
import com.cleanengine.coin.order.application.OrderService;
import com.cleanengine.coin.order.external.adapter.wallet.WalletExternalRepository;
import com.cleanengine.coin.user.domain.Wallet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DisplayName("SellOrder 검증, Repository 반영 통합 테스트")
@Transactional
public class SellOrderIntegrationTest {
    @Autowired
    OrderService orderService;

    @Autowired
    WalletExternalRepository walletRepository;

    @DisplayName("충분한 가상화폐가 있는 유저가 시장가 매도주문 생성시 주문이 생성됨.")
    @Sql("classpath:db/user/user_enough_holdings.sql")
    @Test
    void givenEnoughMoneyUser_WhenCreateMarketSellOrder_ThenSellOrderIsCreated() {
        OrderCommand.CreateOrder command = new OrderCommand.CreateOrder("BTC", 1,
                false, true, 30.0, null, LocalDateTime.now(),false);

        OrderInfo.SellOrderInfo sellOrderInfo = (OrderInfo.SellOrderInfo) orderService.createOrder(command);
        Wallet wallet = walletRepository.findWalletBy(1, "BTC").orElseThrow();

        assertNotNull(sellOrderInfo.getId());
        assertEquals(200000-30.0, wallet.getSize());
    }

    @DisplayName("충분한 가상화폐가 있는 유저가 지정가 매도주문 생성시 주문이 생성됨.")
    @Sql("classpath:db/user/user_enough_holdings.sql")
    @Test
    void givenEnoughMoneyUser_WhenCreateLimitSellOrder_ThenSellOrderIsCreated() {
        OrderCommand.CreateOrder command = new OrderCommand.CreateOrder("BTC", 1,
                false, false, 30.0, 40.0, LocalDateTime.now(),false);

        OrderInfo.SellOrderInfo sellOrderInfo = (OrderInfo.SellOrderInfo) orderService.createOrder(command);
        Wallet wallet = walletRepository.findWalletBy(1, "BTC").orElseThrow();

        assertNotNull(sellOrderInfo.getId());
        assertEquals(200000-30.0, wallet.getSize());
    }

    @DisplayName("가상화폐가 없는 유저가 시장가 매도주문 생성시 DomainValidationException을 반환함.")
    @Sql("classpath:db/user/user_zero_holdings.sql")
    @Test
    void givenZeroMoneyUser_WhenCreateMarketSellOrder_ThenExceptionIsThrown() {
        OrderCommand.CreateOrder command = new OrderCommand.CreateOrder("BTC", 1,
                false, true, 30.0, null, LocalDateTime.now(),false);

        assertThrows(DomainValidationException.class, () -> orderService.createOrder(command));
    }

    @DisplayName("가상화폐가 없는 유저가 지정가 매도주문 생성시 DomainValidationException을 반환함.")
    @Sql("classpath:db/user/user_zero_holdings.sql")
    @Test
    void givenZeroMoneyUser_WhenCreateLimitSellOrder_ThenExceptionIsThrown() {
        OrderCommand.CreateOrder command = new OrderCommand.CreateOrder("BTC", 1,
                false, false, 30.0, 40.0, LocalDateTime.now(),false);

        assertThrows(DomainValidationException.class, () -> orderService.createOrder(command));
    }

    @DisplayName("orderSize를 누락한 시장가 매도주문이 들어올 경우 DomainValidationException을 반환함.")
    @Test
    void givenCommandWithoutOrderSize_WhenCreateMarketSellOrder_ThenExceptionIsThrown() {
        OrderCommand.CreateOrder command = new OrderCommand.CreateOrder("BTC", 1,
                false, true, null, null, LocalDateTime.now(),false);

        assertThrows(DomainValidationException.class, () -> orderService.createOrder(command));
    }

    @DisplayName("price를 누락한 지정가 매도주문이 들어올 경우 DomainValidationException을 반환함.")
    @Test
    void givenCommandWithoutPrice_WhenCreateLimitSellOrder_ThenExceptionIsThrown() {
        OrderCommand.CreateOrder command = new OrderCommand.CreateOrder("BTC", 1,
                false, false, 30.0, null, LocalDateTime.now(),false);

        assertThrows(DomainValidationException.class, () -> orderService.createOrder(command));
    }

    @DisplayName("orderSize를 누락한 지정가 매도주문이 들어올 경우 DomainValidationException을 반환함.")
    @Test
    void givenCommandWithoutOrderSize_WhenCreateLimitSellOrder_ThenExceptionIsThrown() {
        OrderCommand.CreateOrder command = new OrderCommand.CreateOrder("BTC", 1,
                false, false, null, 40.0, LocalDateTime.now(),false);

        assertThrows(DomainValidationException.class, () -> orderService.createOrder(command));
    }
}
