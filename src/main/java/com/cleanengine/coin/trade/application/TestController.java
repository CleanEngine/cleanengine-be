package com.cleanengine.coin.trade.application;


import com.cleanengine.coin.trade.repository.r2dbcRepository.TradeReactiveRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @Autowired
    TradeReactiveRepository tradeReactiveRepository;

    @GetMapping("/api/trade/test")
    public String test() {

        tradeReactiveRepository.findAll().subscribe(System.out::println);

        return "ok";
    }

}
