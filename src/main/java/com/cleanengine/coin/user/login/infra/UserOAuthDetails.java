package com.cleanengine.coin.user.login.infra;

import com.cleanengine.coin.user.domain.OAuth;
import com.cleanengine.coin.user.domain.User;
import lombok.*;

@Getter
@Setter
public class UserOAuthDetails {

    private Integer userId;

    private String provider;

    private String providerUserId;

    private String email;

    private String name;

    @Builder
    private UserOAuthDetails(Integer userId, String provider, String providerUserId, String email, String name) {
        this.userId = userId;
        this.provider = provider;
        this.providerUserId = providerUserId;
        this.email = email;
        this.name = name;
    }

    public static UserOAuthDetails of(User user, OAuth oAuth) {
        return UserOAuthDetails.builder()
                .userId(user.getId())
                .provider(oAuth.getProvider())
                .providerUserId(oAuth.getProviderUserId())
                .email(oAuth.getEmail())
                .name(oAuth.getNickname())
                .build();
    }

    public static UserOAuthDetails of(int userId) {
        return UserOAuthDetails.builder()
                .userId(userId)
                .build();
    }

    public void update(OAuth oauth) {
        this.email = oauth.getEmail();
        this.name = oauth.getNickname();
    }

}
