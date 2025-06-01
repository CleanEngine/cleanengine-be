package com.cleanengine.coin.realitybot.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ApiController {
    //초기 api 작동 확인용 -> realitybot controller로 전환 필요
//    private final BithumbAPIClient bithumbAPIClient;
//    public ApiController(BithumbAPIClient bithumbAPIClient) {
//        this.bithumbAPIClient = bithumbAPIClient;
//    }
//    @GetMapping("/test/{tickerName}")
//    public String getApiData(@PathVariable String tickerName) {
//        System.out.println("tickername 출력"+tickerName);
//        return bithumbAPIClient.get(tickerName);
//    }
}
