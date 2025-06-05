package com.cleanengine.coin.chart.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WebsocketSendService 단위 테스트")
class WebsocketSendServiceTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private WebsocketSendService websocketSendService;

    private Object testData;
    private String testTicker;

    @BeforeEach
    void setUp() {
        testData = new TestDto("BTC", 50000.0, 1.5);
        testTicker = "BTC";
    }

    // ===== buildTopic 메서드 테스트 =====
    @Test
    @DisplayName("buildTopic - 정상적인 토픽 생성 (realTimeTradeRate)")
    void buildTopic_RealTimeTradeRate_ReturnsCorrectTopic() {
        // when
        String result = websocketSendService.buildTopic("realTimeTradeRate", "BTC");

        // then
        assertThat(result).isEqualTo("/topic/realTimeTradeRate/BTC");
    }

    @Test
    @DisplayName("buildTopic - 정상적인 토픽 생성 (prevRate)")
    void buildTopic_PrevRate_ReturnsCorrectTopic() {
        // when
        String result = websocketSendService.buildTopic("prevRate", "ETH");

        // then
        assertThat(result).isEqualTo("/topic/prevRate/ETH");
    }

    @Test
    @DisplayName("buildTopic - 다양한 티커로 정상 생성")
    void buildTopic_DifferentTickers_ReturnsCorrectTopic() {
        // when & then
        assertThat(websocketSendService.buildTopic("realTimeTradeRate", "TRUMP"))
                .isEqualTo("/topic/realTimeTradeRate/TRUMP");
        assertThat(websocketSendService.buildTopic("prevRate", "BTC-USD"))
                .isEqualTo("/topic/prevRate/BTC-USD");
    }

    @Test
    @DisplayName("buildTopic - null 티커인 경우 예외 발생")
    void buildTopic_NullTicker_ThrowsException() {
        // when & then
        assertThatThrownBy(() -> websocketSendService.buildTopic("realTimeTradeRate", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("티커는 비어있을 수 없습니다");
    }

    @Test
    @DisplayName("buildTopic - 빈 문자열 티커인 경우 예외 발생")
    void buildTopic_EmptyTicker_ThrowsException() {
        // when & then
        assertThatThrownBy(() -> websocketSendService.buildTopic("realTimeTradeRate", ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("티커는 비어있을 수 없습니다");
    }

    @Test
    @DisplayName("buildTopic - 공백만 있는 티커인 경우 예외 발생")
    void buildTopic_WhitespaceTicker_ThrowsException() {
        // when & then
        assertThatThrownBy(() -> websocketSendService.buildTopic("realTimeTradeRate", "   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("티커는 비어있을 수 없습니다");
    }

    @Test
    @DisplayName("buildTopic - 탭과 공백이 섞인 티커인 경우 예외 발생")
    void buildTopic_TabAndWhitespaceTicker_ThrowsException() {
        // when & then
        assertThatThrownBy(() -> websocketSendService.buildTopic("realTimeTradeRate", "\t  \n"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("티커는 비어있을 수 없습니다");
    }

    // ===== sendMessage 메서드 테스트 =====
    @Test
    @DisplayName("sendMessage - 정상적인 메시지 전송")
    void sendMessage_ValidInput_SendsMessageCorrectly() {
        // given
        String topic = "/topic/realTimeTradeRate/BTC";

        // when
        websocketSendService.sendMessage(topic, testData);

        // then
        verify(messagingTemplate, times(1)).convertAndSend(topic, testData);
    }

    @Test
    @DisplayName("sendMessage - 다른 토픽으로 정상 전송")
    void sendMessage_DifferentTopic_SendsCorrectly() {
        // given
        String topic = "/topic/prevRate/ETH";
        Object data = "test data";

        // when
        websocketSendService.sendMessage(topic, data);

        // then
        verify(messagingTemplate, times(1)).convertAndSend(topic, data);
    }

    @Test
    @DisplayName("sendMessage - null 토픽인 경우 예외 발생")
    void sendMessage_NullTopic_ThrowsException() {
        // when & then
        assertThatThrownBy(() -> websocketSendService.sendMessage(null, testData))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("토픽은 비어있을 수 없습니다");

        verify(messagingTemplate, never()).convertAndSend((String) any(), (Object) any());
    }

    @Test
    @DisplayName("sendMessage - 빈 토픽인 경우 예외 발생")
    void sendMessage_EmptyTopic_ThrowsException() {
        // when & then
        assertThatThrownBy(() -> websocketSendService.sendMessage("", testData))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("토픽은 비어있을 수 없습니다");

        verify(messagingTemplate, never()).convertAndSend((String) any(), (Object) any());
    }

    @Test
    @DisplayName("sendMessage - 공백만 있는 토픽인 경우 예외 발생")
    void sendMessage_WhitespaceTopic_ThrowsException() {
        // when & then
        assertThatThrownBy(() -> websocketSendService.sendMessage("   ", testData))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("토픽은 비어있을 수 없습니다");

        verify(messagingTemplate, never()).convertAndSend((String) any(), (Object) any());
    }

    @Test
    @DisplayName("sendMessage - null 데이터인 경우 예외 발생")
    void sendMessage_NullData_ThrowsException() {
        // given
        String topic = "/topic/realTimeTradeRate/BTC";

        // when & then
        assertThatThrownBy(() -> websocketSendService.sendMessage(topic, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("데이터는 null일 수 없습니다");

        verify(messagingTemplate, never()).convertAndSend((String) any(), (Object) any());
    }

    // ===== sendChangeRate 메서드 테스트 =====
    @Test
    @DisplayName("sendChangeRate - 정상적인 실시간 거래 데이터 전송")
    void sendChangeRate_ValidInput_SendsCorrectly() {
        // when
        websocketSendService.sendChangeRate(testData, testTicker);

        // then
        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> dataCaptor = ArgumentCaptor.forClass(Object.class);

        verify(messagingTemplate, times(1)).convertAndSend(topicCaptor.capture(), dataCaptor.capture());

        assertThat(topicCaptor.getValue()).isEqualTo("/topic/realTimeTradeRate/BTC");
        assertThat(dataCaptor.getValue()).isEqualTo(testData);
    }

    @Test
    @DisplayName("sendChangeRate - 다양한 티커로 정상 전송")
    void sendChangeRate_DifferentTickers_SendsCorrectly() {
        // given
        Object ethData = new TestDto("ETH", 3000.0, 2.0);

        // when
        websocketSendService.sendChangeRate(ethData, "ETH");

        // then
        verify(messagingTemplate).convertAndSend("/topic/realTimeTradeRate/ETH", ethData);
    }

    @Test
    @DisplayName("sendChangeRate - null 티커인 경우 예외 발생")
    void sendChangeRate_NullTicker_ThrowsException() {
        // when & then
        assertThatThrownBy(() -> websocketSendService.sendChangeRate(testData, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("티커는 비어있을 수 없습니다");

        verify(messagingTemplate, never()).convertAndSend((String) any(), (Object) any());
    }

    @Test
    @DisplayName("sendChangeRate - 빈 티커인 경우 예외 발생")
    void sendChangeRate_EmptyTicker_ThrowsException() {
        // when & then
        assertThatThrownBy(() -> websocketSendService.sendChangeRate(testData, ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("티커는 비어있을 수 없습니다");

        verify(messagingTemplate, never()).convertAndSend((String) any(), (Object) any());
    }

    @Test
    @DisplayName("sendChangeRate - null 데이터인 경우 예외 발생")
    void sendChangeRate_NullData_ThrowsException() {
        // when & then
        assertThatThrownBy(() -> websocketSendService.sendChangeRate(null, testTicker))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("데이터는 null일 수 없습니다");

        verify(messagingTemplate, never()).convertAndSend((String) any(), (Object) any());
    }

    // ===== sendPrevRate 메서드 테스트 =====
    @Test
    @DisplayName("sendPrevRate - 정상적인 전일 대비 데이터 전송")
    void sendPrevRate_ValidInput_SendsCorrectly() {
        // when
        websocketSendService.sendPrevRate(testData, testTicker);

        // then
        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> dataCaptor = ArgumentCaptor.forClass(Object.class);

        verify(messagingTemplate, times(1)).convertAndSend(topicCaptor.capture(), dataCaptor.capture());

        // prevRate 토픽으로 전송됨
        assertThat(topicCaptor.getValue()).isEqualTo("/topic/prevRate/BTC");
        assertThat(dataCaptor.getValue()).isEqualTo(testData);
    }

    @Test
    @DisplayName("sendPrevRate - 다양한 티커로 정상 전송")
    void sendPrevRate_DifferentTickers_SendsCorrectly() {
        // given
        Object trumpData = new TestDto("TRUMP", 150.0, 2.5);

        // when
        websocketSendService.sendPrevRate(trumpData, "TRUMP");

        // then
        verify(messagingTemplate).convertAndSend("/topic/prevRate/TRUMP", trumpData);
    }

    @Test
    @DisplayName("sendPrevRate - null 티커인 경우 예외 발생")
    void sendPrevRate_NullTicker_ThrowsException() {
        // when & then
        assertThatThrownBy(() -> websocketSendService.sendPrevRate(testData, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("티커는 비어있을 수 없습니다");

        verify(messagingTemplate, never()).convertAndSend((String) any(), (Object) any());
    }

    @Test
    @DisplayName("sendPrevRate - 공백 티커인 경우 예외 발생")
    void sendPrevRate_WhitespaceTicker_ThrowsException() {
        // when & then
        assertThatThrownBy(() -> websocketSendService.sendPrevRate(testData, "  "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("티커는 비어있을 수 없습니다");

        verify(messagingTemplate, never()).convertAndSend((String) any(), (Object) any());
    }

    @Test
    @DisplayName("sendPrevRate - null 데이터인 경우 예외 발생")
    void sendPrevRate_NullData_ThrowsException() {
        // when & then
        assertThatThrownBy(() -> websocketSendService.sendPrevRate(null, testTicker))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("데이터는 null일 수 없습니다");

        verify(messagingTemplate, never()).convertAndSend((String) any(), (Object) any());
    }


    // 테스트용 DTO 클래스
    private static class TestDto {
        private final String ticker;
        private final Double price;
        private final Double size;

        public TestDto(String ticker, Double price, Double size) {
            this.ticker = ticker;
            this.price = price;
            this.size = size;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            TestDto testDto = (TestDto) obj;
            return ticker.equals(testDto.ticker) &&
                    price.equals(testDto.price) &&
                    size.equals(testDto.size);
        }

        @Override
        public String toString() {
            return String.format("TestDto{ticker='%s', price=%s, size=%s}", ticker, price, size);
        }
    }
}