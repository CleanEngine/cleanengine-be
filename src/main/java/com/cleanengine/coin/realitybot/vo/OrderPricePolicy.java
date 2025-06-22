package com.cleanengine.coin.realitybot.vo;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import org.springframework.stereotype.Component;

@Component
public class OrderPricePolicy {
    /**
     * 레벨에 따라 매수/매도 가격을 계산합니다.
     * @param level          주문 강도 (1~5)
     * @param platformVWAP   플랫폼 기준 평균 체결 가격
     * @param unitPrice      호가 단위
     * @param trendLineRate  플랫폼과 API VWAP의 편차율
     * @return PricePair (매도/매수 가격)
     */
    @WithSpan("api.request.02.order.platformvwap.orderprice")
    public OrderPrice calculatePrice(int level,
                                     double platformVWAP,
                                     double unitPrice,
                                     double trendLineRate) {
        double priceOffset = unitPrice * level;
        double sellPrice, buyPrice;
        double randomOffset =  Math.abs(getRandomOffset(platformVWAP,getDynamicMaxRate(trendLineRate)));
        double basePrice = normalizeToUnit(platformVWAP, unitPrice); //기준 가격 (호가 단위 정규화)


        if (level == 1){ //1level일 경우 주문이 겹치도록 설정
            //체결을 위해 매수가 올리고, 매도가 내리는 계산 적용
            sellPrice = normalizeToUnit(basePrice - randomOffset,unitPrice);
            buyPrice = normalizeToUnit(basePrice + randomOffset,unitPrice);
        }
        //2~3 단계 : orderbook 단위 주문
        else {
            randomOffset =  getRandomOffset(platformVWAP,0.01);
            //체결 확률 증가용 코드
            sellPrice = normalizeToUnit(platformVWAP + priceOffset - randomOffset,unitPrice);
            buyPrice = normalizeToUnit(platformVWAP - priceOffset + randomOffset,unitPrice);
            //안정적인 스프레드 유지
//                sellPrice = normalizeToUnit(platformVWAP + priceOffset);
//                buyPrice = normalizeToUnit(platformVWAP - priceOffset);
        }
        return new OrderPrice(sellPrice, buyPrice);
    }

    private double getRandomOffset(double basePrice, double maxRate){
        //시장가에 해당하는 호가는 거래 체결 강하게 하기 위함
        double percent = (Math.random() * 2-1)*maxRate;
        return basePrice * percent;
    }

    private double getDynamicMaxRate(double trendLineRate) {
        // 편차가 벌어지면 벌어질수록 보정폭 확대
        // 5% = 2.51의 가중치
        // 11% = 5.51의 가중치
        return 0.01 + Math.abs(trendLineRate) * 0.5;
    }

    private double normalizeToUnit(double price, double unitPrice){ //호가단위로 변환
        double normalized = Math.round(price / unitPrice) * unitPrice;
        return Math.max(normalized, unitPrice); // 최소한 1틱 이상으로 보정
    }

    public record OrderPrice(double sell, double buy){}
}
