package com.cleanengine.coin.realitybot.vo;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@NoArgsConstructor
public class DeviationPricePolicy {
    /**
     * 편차율이 클 경우 가격과 수량을 강하게 보정합니다.
     *
     * @param platformSell 계산 된 플랫폼 기준 매도 가격
     * @param platformBuy 계산 된 플랫폼 기준 매수 가격
     * @param trendLineRate (platformVWAP - apiVWAP) / apiVWAP
     * @param apiVWAP 외부 기준 가격
     * @return 추가 선형 보정된 가격쌍 (sell, buy)
     */
    @WithSpan("api.request.02.order.platformvwap.deviationprice")
    public AdjustPrice adjust(double platformSell,double platformBuy, double trendLineRate, double apiVWAP, double unitPrice){
        double deviation = Math.abs(trendLineRate);//음수값 보정
        if (deviation <= 0.017){
            return new AdjustPrice(platformSell,platformBuy);
        }
        double weight = getCorrectionWeight(deviation);
//            double closeness = 1-weight; // 보간 가중치: 0.7 ~ 1.0 -> 0.5
            double closeness; // 보간 가중치: 0.7 ~ 1.0 -> 0.5
        if (deviation > 0.07){
             closeness = Math.max(0.2, 1 - weight);
        } else {
            closeness = 0.01;
        }

//        double targetVWAP = (trendLineRate > 0) //만약 closeness 를 0.5 입력시 중간값
//                ? apiVWAP + (platformSell - apiVWAP) * closeness  // 고평가 → platformSell(25000) → apiVWAP(16000) 사이 가중치 %로 유도
//                : apiVWAP - (apiVWAP - platformBuy) * closeness; // 저평가 → platformBuy(12000) ← apiVWAP(16000) 사이 가중치 %로 유도
        double sellTarget, buyTarget;
        if (trendLineRate > 0) {
            // sell은 platformSell에서 apiVWAP 쪽으로 낮춤
             sellTarget = apiVWAP + (platformSell - apiVWAP) * closeness;
             buyTarget  = apiVWAP + (platformBuy - apiVWAP) * closeness;
        } else {
            // 저평가일 경우: 가격을 올림
             sellTarget = apiVWAP - (apiVWAP - platformSell) * closeness;
             buyTarget  = apiVWAP - (apiVWAP - platformBuy) * closeness;
            }

//        double adjustedSell = normalizeToUnit(sellTarget,unitPrice);
//        double adjustedBuy = normalizeToUnit(buyTarget,unitPrice);
        double adjustedSell = normalizeToUnit(interpolate(platformSell,sellTarget ,weight),unitPrice);
        double adjustedBuy = normalizeToUnit(interpolate(platformBuy,buyTarget ,weight),unitPrice);
        return new AdjustPrice(adjustedSell,adjustedBuy);

    }

    /*private double getCorrentionRate(double deviation) { 3단계 보정에서 선형보정
        if (deviation <= 0.01){
            return 0.05; //5% 약보정
        } else if (deviation <= 0.03){
            return 0.10; //10% 의 중보정
        } else return 0.15; //15%의 강보정
    }*/

    /**
     * 1% 미만은 보정 X, 10% 이상은 거의 전면 보정.
     * 중간값은 비례적으로 weight 증가
     */
    private double getCorrectionWeight(double deviation) {
        double start = 0.01;  // 보정 시작 기준 (1%)
        double end   = 0.10;  // 보정 최댓값 기준 (10%)

        double weight = (deviation - start) / (end - start);
        return Math.min(1.0, Math.max(0.0, weight)); // 0 ~ 1 사이로 제한
    }

    /**
     * 선형 보간 함수: platformPrice → apiVWAP 사이 보간
     */
    private double interpolate(double platformPrice, double apiVWAP, double weight) {
        double interWeight = Math.min(1.0,weight*1.2);
        return platformPrice * (1 - interWeight) + apiVWAP * interWeight;
    }
    private double normalizeToUnit(double price, double unitPrice) {
        double normalized = Math.round(price / unitPrice) * unitPrice;
        return Math.max(normalized, unitPrice); // 최소한 1틱 이상으로 보정
    }

    public record AdjustPrice(double sell, double buy){}
}
