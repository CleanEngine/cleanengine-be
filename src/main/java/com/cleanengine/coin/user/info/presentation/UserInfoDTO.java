package com.cleanengine.coin.user.info.presentation;

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

    private Double cash;

}
