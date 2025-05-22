package com.cleanengine.coin.user.info.presentation;

import com.cleanengine.coin.user.domain.Wallet;
import com.cleanengine.coin.user.info.application.PlainDoubleSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoDTO {

    private Integer userId;

    private String email;

    private String nickname;

    private String provider;

    @JsonSerialize(using = PlainDoubleSerializer.class)
    private Double cash;

    private List<Wallet> wallets;

}
