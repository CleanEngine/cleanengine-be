package com.cleanengine.coin.realitybot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RealityBotApplication {
    public static void main(String[] args) {
        SpringApplication.run(RealityBotApplication.class, args);
    }
}
