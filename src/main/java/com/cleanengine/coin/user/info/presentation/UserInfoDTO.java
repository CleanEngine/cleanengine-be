package com.cleanengine.coin.user.info.presentation;

import com.cleanengine.coin.user.domain.Wallet;
import com.cleanengine.coin.user.info.application.PlainDoubleSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class UserInfoDTO {

    private Integer userId;

    private String email;

    private String nickname;

    private String provider;

    @JsonSerialize(using = PlainDoubleSerializer.class)
    private Double cash;

    private List<Wallet> wallets;

    private UserInfoDTO(Integer userId, String email, String nickname, String provider, Double cash, List<Wallet> wallets) {
        this.userId = userId;
        this.email = email;
        this.nickname = nickname;
        this.provider = provider;
        this.cash = cash;
        this.wallets = wallets;
    }

    public static UserInfoDTO of(Integer userId, String email, String nickname, String provider, Double cash, List<Wallet> wallets) {
        return new UserInfoDTO(userId, email, nickname, provider, cash, wallets);
    }

}
