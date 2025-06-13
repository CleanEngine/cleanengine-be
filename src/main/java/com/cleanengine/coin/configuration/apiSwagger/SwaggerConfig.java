package com.cleanengine.coin.configuration.apiSwagger;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        // API 기본 정보 설정
        Info info = new Info()
                .title("InvestFuture API Document")
                .version("1.0")
                .description("Private API 호출 시 Cookie에 직접 설정해주세요!\n")
                .contact(new io.swagger.v3.oas.models.info.Contact().email("billage.official@gmail.com"));

        // Swagger UI 설정 및 보안 추가
        return new OpenAPI()
                .addServersItem(new Server().url("http://localhost:8080"))  // 추가적인 서버 URL 설정 가능
                .info(info);
    }

}