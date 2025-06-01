package com.cleanengine.coin.realitybot.config;

import okhttp3.OkHttpClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ApiClientConfig.class)
public class ApiClientConfigTest {

    @Autowired
    OkHttpClient okHttpClient;

    @DisplayName("Bean 정상 등록 여부")
    @Test
    void CreateokHttpClientBean() {
        assertNotNull(okHttpClient);
    }

}