package com.cleanengine.coin.order.integration.buyorder;

import com.cleanengine.coin.common.error.DomainValidationException;
import com.cleanengine.coin.order.application.OrderCommand;
import com.cleanengine.coin.order.application.OrderInfo;
import com.cleanengine.coin.order.application.OrderService;
import com.cleanengine.coin.order.external.adapter.account.AccountExternalRepository;
import com.cleanengine.coin.order.external.adapter.wallet.WalletExternalRepository;
import com.cleanengine.coin.user.domain.Account;
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
@DisplayName("BuyOrder 검증, Repository 반영 통합 테스트")
@Transactional
public class BuyOrderIntegrationTest {
    @Autowired
    OrderService orderService;

    @Autowired
    AccountExternalRepository accountRepository;

    @Autowired
    WalletExternalRepository walletRepository;

    //TODO 3,2가 예약어로 사용하는 만큼 1을 insert하는 테스트가 깨질 수 있다. 또한, sql로  초기화보다 EntityManager나 Repository로 초기화하는게 나은듯
    @DisplayName("충분한 돈이 있는 유저가 시장가 매수주문 생성시 주문이 정상 생성됨.")
    @Sql("classpath:db/user/user_enough_holdings.sql")
    @Test
    void givenEnoughMoneyUser_WhenCreateMarketBuyOrder_ThenBuyOrderIsCreated() {
        OrderCommand.CreateOrder command = new OrderCommand.CreateOrder("BTC", 3,
                true, true, null, 30.0, LocalDateTime.now(),false);

        OrderInfo.BuyOrderInfo buyOrderInfo = (OrderInfo.BuyOrderInfo) orderService.createOrder(command);
        Account account = accountRepository.findByUserId(3).orElseThrow();

        assertNotNull(buyOrderInfo.getId());
        assertEquals(200000-30.0, account.getCash());
    }

    @DisplayName("충분한 돈이 있는 유저가 지정가 매수주문 생성시 주문이 생성됨.")
    @Sql("classpath:db/user/user_enough_holdings.sql")
    @Test
    void givenEnoughMoneyUser_WhenCreateLimitBuyOrder_ThenSellOrderIsCreated() {
        OrderCommand.CreateOrder command = new OrderCommand.CreateOrder("BTC", 3,
                true, false, 30.0, 40.0, LocalDateTime.now(),false);

        OrderInfo.BuyOrderInfo buyOrderInfo = (OrderInfo.BuyOrderInfo) orderService.createOrder(command);
        Account account = accountRepository.findByUserId(3).orElseThrow();

        assertNotNull(buyOrderInfo.getId());
        assertEquals(200000-30.0*40.0, account.getCash());
    }

    @DisplayName("돈이 없는 유저가 시장가 매수주문 생성시 DomainValidationException을 반환함.")
    @Sql("classpath:db/user/user_zero_holdings.sql")
    @Test
    void givenZeroMoneyUser_WhenCreateMarketBuyOrder_ThenExceptionIsThrown() {
        OrderCommand.CreateOrder command = new OrderCommand.CreateOrder("BTC", 3,
                true, true, null, 40.0, LocalDateTime.now(),false);

        assertThrows(DomainValidationException.class, () -> orderService.createOrder(command));
    }

    @DisplayName("돈이 없는 유저가 지정가 매수주문 생성시 DomainValidationException을 반환함.")
    @Sql("classpath:db/user/user_zero_holdings.sql")
    @Test
    void givenZeroMoneyUser_WhenCreateLimitBuyOrder_ThenExceptionIsThrown() {
        OrderCommand.CreateOrder command = new OrderCommand.CreateOrder("BTC", 3,
                true, false, 30.0, 40.0, LocalDateTime.now(),false);

        assertThrows(DomainValidationException.class, () -> orderService.createOrder(command));
    }

    @DisplayName("price를 누락한 시장가 매수주문이 들어올 경우 DomainValidationException을 반환함.")
    @Test
    void givenCommandWithoutPrice_WhenCreateMarketBuyOrder_ThenExceptionIsThrown() {
        OrderCommand.CreateOrder command = new OrderCommand.CreateOrder("BTC", 3,
                true, true, null, null, LocalDateTime.now(),false);

        assertThrows(DomainValidationException.class, () -> orderService.createOrder(command));
    }

    @DisplayName("price를 누락한 지정가 매수주문이 들어올 경우 DomainValidationException을 반환함.")
    @Test
    void givenCommandWithoutPrice_WhenCreateLimitBuyOrder_ThenExceptionIsThrown() {
        OrderCommand.CreateOrder command = new OrderCommand.CreateOrder("BTC", 3,
                true, false, 30.0, null, LocalDateTime.now(),false);

        assertThrows(DomainValidationException.class, () -> orderService.createOrder(command));
    }

    @DisplayName("orderSize를 누락한 지정가 매수주문이 들어올 경우 DomainValidationException을 반환함.")
    @Test
    void givenCommandWithoutOrderSize_WhenCreateLimitBuyOrder_ThenExceptionIsThrown() {
        OrderCommand.CreateOrder command = new OrderCommand.CreateOrder("BTC", 3,
                true, false, null, 40.0, LocalDateTime.now(),false);

        assertThrows(DomainValidationException.class, () -> orderService.createOrder(command));
    }

    @DisplayName("Wallet이 없는 사용자가 주문 요청을 할 경우 Wallet이 생성된다.")
    @Sql("classpath:db/user/user_without_wallet.sql")
    @Test
    void givenUserWithoutWallet_WhenCreateOrder_ThenWalletIsCreated() {
        OrderCommand.CreateOrder command = new OrderCommand.CreateOrder("BTC", 3,
                true, false, 30.0, 40.0, LocalDateTime.now(),false);

        orderService.createOrder(command);

        Wallet wallet = walletRepository.findWalletBy(3, "BTC").orElseThrow();
        assertNotNull(wallet);
        assertEquals("BTC", wallet.getTicker());
    }
}
