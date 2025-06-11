package com.cleanengine.coin.realitybot.vo;

import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@NoArgsConstructor
public class OrderVolumePolicy {

    /**
     * 평균 거래량과 추세 편차율을 기반으로 랜덤 거래량을 생성합니다.
     *
     * @param avgVolume 평균 거래량
     * @param trendLineRate platformVWAP - apiVWAP 편차율 (e.g., 0.03 = +3%)
     * @param isBuy 매수면 true, 매도면 false
     * @return 생성된 거래량
     */

    public double calculateVolume(double avgVolume, double trendLineRate, boolean isBuy){
        //기본 랜덤 거래량 (0.5~1.5)
        double rawVolume = avgVolume *(0.5*Math.random());

        //편차에 따른 거래량 보정 3% -> 최대 2.5배 증가
        double deviation = Math.abs(trendLineRate); //절댓값 반환
        double power = deviation * 100; //0.03 -> 3%
        double multiplier;
        if (deviation >= 0.1){//1% 초과할 경우
            multiplier = 1.0 + (power * 0.5); //2.5배 (max로 사용)
        } else if (deviation >= 0.01){
            double baseline = 5.0-((deviation - 0.01)/0.09)*2.0;
            multiplier = baseline + (power * 0.5);
        } else {
            multiplier = 1.0 + (power * 0.5);
        }
//            double multiplier = Math.pow(1.2,power); //2.5배 (max로 사용)
            rawVolume *= multiplier; //강한 추세 -> 강한 보정


        //매수-매도 비중 조정
        if (deviation <=0.001) //0.1%일 경우 안정권 , 추가적인 보정 x
            return volumeExpansion(rawVolume);
        if (trendLineRate > 0){
            //시장이 상승하면 매도 강세보정
            return isBuy? volumeExpansion(rawVolume* 0.7) //소극적 매수
                        : volumeExpansion(rawVolume*1.5); //적극적 매도
        } else {
            //시장이 하락하면 매수 강세보정
            return isBuy? volumeExpansion(rawVolume*1.5) //적극적 매도
                        : volumeExpansion(rawVolume*0.7); //소극적 매수
        }
    }

    private double volumeExpansion(double rawVolume){
        double resultVolume = Math.round(rawVolume * 10000.0)/10000.0;
        if(resultVolume <= 0) {
            //Volume이 0이하일 경우 재 계산
            resultVolume = Math.round(rawVolume * 10000000.0) / 10000000.0;
            if(resultVolume <= 0){
                resultVolume = 0.0000001;
            }
        }
        return resultVolume;
    }
}
