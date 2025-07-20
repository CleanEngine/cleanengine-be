package com.cleanengine.coin.user.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "oauth")
@NoArgsConstructor
public class OAuth {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "oauth_id")
    private Integer id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "provider", nullable = false, length = 20)
    private String provider;

    @Column(name = "provider_user_id", nullable = false)
    private String providerUserId;

    @Column(name = "email")
    private String email;

    @Column(name = "nickname")
    private String nickname;

    @Column(name = "access_token", columnDefinition = "TEXT")
    private String accessToken;

    @Column(name = "refresh_token", columnDefinition = "TEXT")
    private String refreshToken;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "scope")
    private String scope;

    @Builder
    private OAuth(Integer userId, String provider, String providerUserId, String email, String nickname) {
        this.userId = userId;
        this.provider = provider;
        this.providerUserId = providerUserId;
        this.email = email;
        this.nickname = nickname;
    }

    public static OAuth of(Integer userId, String provider, String providerUserId, String email, String nickname) {
        return OAuth.builder()
                .userId(userId)
                .provider(provider)
                .providerUserId(providerUserId)
                .email(email)
                .nickname(nickname)
                .build();
    }

}
