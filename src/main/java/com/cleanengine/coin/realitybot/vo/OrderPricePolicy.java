package com.cleanengine.coin.realitybot.vo;

import org.springframework.stereotype.Component;

@Component
public class OrderPricePolicy {
    /**
     * 레벨에 따라 매수/매도 가격을 계산합니다.
     * @param level          주문 강도 (1~5)
//     * @param platformVWAP   플랫폼 기준 평균 체결 가격
     * @param unitPrice      호가 단위
//     * @param trendLineRate  플랫폼과 API VWAP의 편차율
     * @return PricePair (매도/매수 가격)
     */
    public OrderPrice calculatePrice(int level, double apiVWAP, double platformVWAP, double unitPrice) {
        double basePrice = normalizeToUnit(apiVWAP, unitPrice);
        double targetPrice = normalizeToUnit(platformVWAP, unitPrice);

        // 🔥 점진적 접근: API VWAP에서 Platform VWAP로 20% 씩 이동
        double convergenceRate = 0.2; // 천천히 접근
        double adjustedBase = basePrice + (targetPrice - basePrice) * convergenceRate;

        double priceOffset = unitPrice * level;

        if (level == 1) {
            // 1레벨: 조정된 기준가 근처 체결 유도
            return new OrderPrice(adjustedBase + priceOffset/2, adjustedBase - priceOffset/2);
        } else {
            // 2~5레벨: 조정된 기준가 기준 스프레드
            return new OrderPrice(adjustedBase + priceOffset, adjustedBase - priceOffset);
        }
    }

    private double getRandomOffset(double basePrice, double maxRate) {
        double percent = (Math.random() * 2 - 1) * maxRate;
        return basePrice * percent;
    }

    private double normalizeToUnit(double price, double unitPrice) {
        return Math.round(price / unitPrice) * unitPrice;
    }

    public record OrderPrice(double sell, double buy) {}
}