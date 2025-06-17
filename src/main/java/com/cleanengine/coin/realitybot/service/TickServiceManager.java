package com.cleanengine.coin.realitybot.service;

import com.cleanengine.coin.realitybot.domain.APIVWAPState;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TickServiceManager {
    /*해당 코드는 ticker 별로 apiVWAP을 계산하기 위해 만들어진 코드입니다.
    * 초기엔 전역에서 vwap을 계산하거나 sequentialid를 변수에 담았으나 인스턴스가 종목별로 생성되어야 해서 작성되었습니다.
    * ConcurrentHashMap을 통해 중복 검사 후 종목명으로 만들어진 게 없다면 새로 만듭니다.
    * */
    private final Map<String, APIVWAPState> tickServiceMap = new ConcurrentHashMap<>();
    @WithSpan("api.request.01.market.apivwap.maker")
    public APIVWAPState getService(String ticker) {
        return tickServiceMap.computeIfAbsent(ticker, t -> new APIVWAPState());
    }
}
