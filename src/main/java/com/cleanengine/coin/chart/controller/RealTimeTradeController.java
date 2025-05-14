package com.cleanengine.coin.chart.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class RealTimeTradeController {

    private final SimpMessagingTemplate messagingTemplate;

}
