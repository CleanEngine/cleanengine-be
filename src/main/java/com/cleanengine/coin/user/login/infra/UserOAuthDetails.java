package com.cleanengine.coin.user.login.infra;

import com.cleanengine.coin.user.domain.OAuth;
import com.cleanengine.coin.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserOAuthDetails {

    private Integer userId;

    private String provider;

    private String providerUserId;

    private String email;

    private String name;

    public UserOAuthDetails(User user, OAuth oAuth) {
        this.userId = user.getId();
        this.provider = oAuth.getProvider();
        this.providerUserId = oAuth.getProviderUserId();
        this.email = oAuth.getEmail();
        this.name = oAuth.getNickname();
    }
}
