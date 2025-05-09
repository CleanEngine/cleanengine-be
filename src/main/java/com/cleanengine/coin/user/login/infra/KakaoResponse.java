package com.cleanengine.coin.user.login.infra;


import java.util.Map;

public class KakaoResponse implements OAuth2Response {

    private final String id;

    private final Map<String, Object> kakaoAccount;

    public KakaoResponse(Map<String, Object> attribute) {
        System.out.println(attribute);
        this.id = attribute.get("id").toString();
        this.kakaoAccount = (Map<String, Object>) attribute.get("kakao_account");
    }

    @Override
    public String getName() {
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
        return profile.get("nickname").toString();
    }

    @Override
    public String getEmail() {
        return kakaoAccount.get("email").toString();
    }

    @Override
    public String getProviderId() {
        return this.id;
    }

    @Override
    public String getProvider() {
        return "kakao";
    }

}
