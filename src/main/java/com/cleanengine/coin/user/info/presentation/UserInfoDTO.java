package com.cleanengine.coin.user.info.presentation;

import com.cleanengine.coin.user.domain.Wallet;
import com.cleanengine.coin.user.info.application.PlainDoubleSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class UserInfoDTO {

    private Integer userId;

    private String email;

    private String nickname;

    private String provider;

    @JsonSerialize(using = PlainDoubleSerializer.class)
    private Double cash;

    @JsonSerialize(using = PlainDoubleSerializer.class)
    private Double totalAssetAmount; // 총 자산(cash + wallets 현재가 * 수량)

    private List<Wallet> wallets;

    private UserInfoDTO(int userId, String email, String nickname, String provider, double cash, List<Wallet> wallets, double totalAssetAmount) {
        this.userId = userId;
        this.email = email;
        this.nickname = nickname;
        this.provider = provider;
        this.cash = cash;
        this.wallets = wallets;
        this.totalAssetAmount = totalAssetAmount;
    }

    public static UserInfoDTO of(int userId, String email, String nickname, String provider, double cash, List<Wallet> wallets, double totalCash) {
        return new UserInfoDTO(userId, email, nickname, provider, cash, wallets, totalCash);
    }

}
