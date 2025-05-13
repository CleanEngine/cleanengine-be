package com.cleanengine.coin.user.info.presentation;

import com.cleanengine.coin.user.info.application.PlainDoubleSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;

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

}
