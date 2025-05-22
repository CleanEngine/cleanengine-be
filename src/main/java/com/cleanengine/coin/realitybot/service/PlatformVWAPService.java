package com.cleanengine.coin.realitybot.service;

import com.cleanengine.coin.realitybot.vo.VWAPState;
import com.cleanengine.coin.trade.entity.Trade;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class PlatformVWAPService {//TODO 가상 시장 조회용 사라질 예정임
    Map<String,VWAPState> vwapMap = new ConcurrentHashMap<>();

    public double calculateVWAPbyTrades(String ticker,List<Trade> trades,double apiVWAP) {
        VWAPState state = vwapMap.computeIfAbsent(ticker,VWAPState::new);
        if (state.getVWAP()==0){
//            state.setVwap(generateVWAP(apiVWAP));
            return generateVWAP(apiVWAP);

        }
        for (Trade trade : trades) {
            state.recordTrade(trade.getPrice(),trade.getSize());
        }
        return state.getVWAP();
    }

    public double generateVWAP(double apiVWAP) {
        log.info("generateVWAP이 실행되었습니다 : {}",apiVWAP);
        double maxDeviationaRate = 0.001; //보정값 0.1%만
        double deviation = (Math.random() * 2 - 1)* maxDeviationaRate; //편차 계산
        return apiVWAP * (1+deviation); // +=deviation 난수 생성 후 계산 (범위는 -1~+1)
    }


}

