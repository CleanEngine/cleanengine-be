package com.cleanengine.coin.user.info.application;

import com.cleanengine.coin.common.CommonValues;
import com.cleanengine.coin.common.domain.port.PriorityQueueStore;
import com.cleanengine.coin.order.application.AssetService;
import com.cleanengine.coin.order.application.OrderCancelService;
import com.cleanengine.coin.order.application.OrderService;
import com.cleanengine.coin.order.application.dto.OrderCommand;
import com.cleanengine.coin.order.domain.BuyOrder;
import com.cleanengine.coin.order.domain.OrderType;
import com.cleanengine.coin.order.domain.SellOrder;
import com.cleanengine.coin.order.domain.spi.ActiveOrdersManager;
import com.cleanengine.coin.order.domain.spi.WaitingOrders;
import com.cleanengine.coin.order.domain.spi.WaitingOrdersManager;
import com.cleanengine.coin.user.domain.Account;
import com.cleanengine.coin.user.domain.Wallet;
import com.cleanengine.coin.user.info.infra.WalletRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@ActiveProfiles({"dev", "it", "h2-mem"})
@DisplayName("계좌 서비스 - h2 통합테스트")
@SpringBootTest
class AccountServiceH2IntegrationTest {

    private static final String TICKER = "BTC";

    @Autowired
    private AccountService accountService;

    @Autowired
    private WalletService walletService;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private OrderService orderService;

    @Autowired
    private WaitingOrdersManager waitingOrdersManager;

    @Autowired
    private ActiveOrdersManager activeOrdersManager;

    @Autowired
    private OrderCancelService orderCancelService;

    @Autowired
    private EntityManager em;

    @Autowired
    private AssetService assetService;

    @Value("${account.initial-cash.user}")
    private double initialCashUser;

    @AfterEach
    public void cleanUpInMemory() {
        waitingOrdersManager.removeWaitingOrders(TICKER);
        activeOrdersManager.removeActiveOrders(TICKER);

        if (!TestTransaction.isActive())
            TestTransaction.start();
        TestTransaction.flagForCommit();
        em.createNativeQuery("TRUNCATE TABLE buy_orders").executeUpdate();
        em.createNativeQuery("TRUNCATE TABLE sell_orders").executeUpdate();
        em.flush();
        em.clear();
        TestTransaction.end();
    }

    @DisplayName("유저 ID와 예수금으로 신규 계좌를 생성한다.")
    @Test
    void createNewAccount() {
        // given
        int userId = 3;
        double cash = CommonValues.INITIAL_USER_CASH;

        // when
        Account account = accountService.createNewAccount(userId, cash);
        assertThat(account).isNotNull();

        Account retrievedAccount = accountService.retrieveAccountByUserId(userId);

        // then
        assertThat(retrievedAccount).isNotNull()
                .extracting(Account::getUserId, Account::getCash)
                .containsExactly(userId, cash);
    }

    @DisplayName("존재하지 않는 userId로 조회 시 null을 반환한다.")
    @Test
    void retrieveAccountByInvalidUserId() {
        // given, when
        Account account = accountService.retrieveAccountByUserId(Integer.MAX_VALUE - 1);

        // then
        assertThat(account).isNull();
    }

    @DisplayName("특정 사용자가 자산을 초기화시키면 자산과 지갑이 초기화되고, 요청했던 주문도 취소된다.")
    @Test
    void resetWithWallets_successfullyResetsAccountAndWalletsAndOrders() {
        // given
        assetService.initAssetCache();

        // Order 커밋용 트랜잭션 생성
        TestTransaction.flagForCommit();

        Integer userId = 4;
        Account account = accountService.createNewAccount(userId, 500000.0);
        walletService.createNewWallets(account.getId());

        Wallet wallet = walletRepository.findByAccountIdAndTicker(account.getId(), TICKER).orElseThrow();
        wallet.increaseSize(100.0);
        walletService.save(wallet);

        em.flush();
        em.clear();
        TestTransaction.end();


        TestTransaction.start();
        TestTransaction.flagForCommit();
        Wallet wallet2 = walletRepository.findByAccountIdAndTicker(account.getId(), TICKER).orElseThrow();
        System.out.println(wallet2.getTicker() + " : " + wallet2.getSize());
        System.out.println("accountId : " + account.getId() + " : " + account.getCash());


        OrderCommand.CreateOrder buyOrderCommand2 = createLimitOrderCommand(true,
                userId, 100.0, 90.0);
        orderService.createOrder(buyOrderCommand2);

        OrderCommand.CreateOrder sellOrderCommand2 = createLimitOrderCommand(false,
                userId, 50.0, 110.0);
        orderService.createOrder(sellOrderCommand2);

        em.flush();
        em.clear();
        TestTransaction.end();
        TestTransaction.start();

        // when
        accountService.resetWithWallets(userId);
        orderCancelService.cancelAllForReset(userId);

        // then
        assertThat(accountService.retrieveAccountByUserId(userId).getCash()).isEqualTo(initialCashUser);
        assertThat(walletService.findWalletByUserIdAndTicker(userId, TICKER).getSize()).isEqualTo(0.0);

        WaitingOrders waitingOrders = waitingOrdersManager.getWaitingOrders(TICKER);
        PriorityQueueStore<BuyOrder> buyOrderPriorityQueueStore = waitingOrders.getBuyOrderPriorityQueueStore(OrderType.LIMIT);
        PriorityQueueStore<SellOrder> sellOrderPriorityQueueStore = waitingOrders.getSellOrderPriorityQueueStore(OrderType.LIMIT);
        assertThat(buyOrderPriorityQueueStore.isEmpty()).isTrue();
        assertThat(sellOrderPriorityQueueStore.isEmpty()).isTrue();
    }

    private OrderCommand.CreateOrder createLimitOrderCommand(boolean isBuyOrder, Integer userId,
                                                             Double orderSize, Double price) {
        return new OrderCommand.CreateOrder(
                TICKER,
                userId,
                isBuyOrder,
                false,
                orderSize,
                price,
                false
        );
    }

}
