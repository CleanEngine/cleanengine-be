package com.cleanengine.coin.trade.application;

import com.cleanengine.coin.common.error.BusinessException;
import com.cleanengine.coin.common.response.ErrorStatus;
import com.cleanengine.coin.order.adapter.out.persistentce.order.command.BuyOrderRepository;
import com.cleanengine.coin.order.adapter.out.persistentce.order.command.SellOrderRepository;
import com.cleanengine.coin.order.domain.Order;
import com.cleanengine.coin.trade.repository.TradeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith( MockitoExtension.class)
class TradeServiceTest {

    @Mock
    TradeRepository tradeRepository;
    @Mock
    BuyOrderRepository buyOrderRepository;
    @Mock
    SellOrderRepository sellOrderRepository;

    @InjectMocks
    TradeService tradeService;

    @DisplayName("매도/매수 주문이 아닌 주문 타입을 변경하려고 하면 예외를 발생시킨다.")
    @Test
    void unsupportedOrder() {
        // given
        class UnsupportedOrder extends Order {}

        // when
        UnsupportedOrder unsupportedOrder = new UnsupportedOrder();

        // then
        assertThatThrownBy(() -> tradeService.updateOrder(unsupportedOrder))
                .isInstanceOf(BusinessException.class)
                        .hasMessage("Unsupported order type: " + unsupportedOrder.getClass().getName(), ErrorStatus.INTERNAL_SERVER_ERROR);
    }
}
