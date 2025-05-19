package com.cleanengine.coin.realitybot.controller;

import com.cleanengine.coin.realitybot.api.BithumbAPIClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ApiController {
    private final BithumbAPIClient bithumbAPIClient;
    public ApiController(BithumbAPIClient bithumbAPIClient) {
        this.bithumbAPIClient = bithumbAPIClient;
    }
    @GetMapping("/test")
    public String getApiData(){
        return bithumbAPIClient.get();

    }}
