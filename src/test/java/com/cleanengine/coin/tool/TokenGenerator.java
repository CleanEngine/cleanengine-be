package com.cleanengine.coin.tool;

import com.cleanengine.coin.user.login.application.JWTUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Disabled
@SpringBootTest
@Profile("dev, it")
public class TokenGenerator {

    @Value("${JWT_SECRET}")
    private String secret;

    @Test
    public void createToken(){
        JWTUtil jwtUtil = new JWTUtil(secret);
        System.out.println(jwtUtil.createJwt(1001, 1000 * 60*60*24*365L));
        System.out.println(jwtUtil.createJwt(1002, 1000 * 60*60*24*365L));
    }

    @Test
    public void createTokens(){
        int size = 0;
        List<String> tokens = new ArrayList<>();

        JWTUtil jwtUtil = new JWTUtil(secret);

        for (int i = 1; i<=1000; i++){
            tokens.add(jwtUtil.createJwt(i, 1000 * 60*60*24*365L));
            size++;
        }

        ObjectMapper objectMapper = new ObjectMapper();

        try{
            File outputFile = new File("tokens.json");
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(outputFile, new UserTokens(size, tokens));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static class UserTokens{
        public int size;
        public List<String> tokens;

        public UserTokens(int size, List<String> tokens) {
            this.size = size;
            this.tokens = tokens;
        }
    }
}
