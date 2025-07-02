package com.cleanengine.coin.user.info.presentation;

import com.cleanengine.coin.user.info.application.PlainDoubleSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class UserInfoDTO {

    @Schema(description = "User ID", example = "3")
    private Integer userId;

    @Schema(description = "이메일", example = "a@a.com")
    private String email;

    @Schema(description = "닉네임", example = "버황")
    private String nickname;

    @Schema(description = "oauth 제공자", example = "kakao")
    private String provider;

    @Schema(description = "예수금", example = "50000000")
    @JsonSerialize(using = PlainDoubleSerializer.class)
    private Double cash;

    @Schema(description = "총 자산", example = "500000000")
    @JsonSerialize(using = PlainDoubleSerializer.class)
    private Double totalAssetAmount; // 총 자산(cash + wallets 현재가 * 수량)

    @Schema(description = "보유 지갑", example = "[{ticker: BTC, size: 10000, buyPrice: 10000000, roi: 0.001}]")
    private List<UserWalletDTO> wallets;

    private UserInfoDTO(int userId, String email, String nickname, String provider, double cash, List<UserWalletDTO> wallets, double totalAssetAmount) {
        this.userId = userId;
        this.email = email;
        this.nickname = nickname;
        this.provider = provider;
        this.cash = cash;
        this.wallets = wallets;
        this.totalAssetAmount = totalAssetAmount;
    }

    public static UserInfoDTO of(int userId, String email, String nickname, String provider, double cash, List<UserWalletDTO> wallets, double totalCash) {
        return new UserInfoDTO(userId, email, nickname, provider, cash, wallets, totalCash);
    }

}
