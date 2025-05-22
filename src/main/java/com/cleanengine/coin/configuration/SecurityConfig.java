package com.cleanengine.coin.configuration;

import com.cleanengine.coin.configuration.SecurityEndpoints.EndpointConfig;
import com.cleanengine.coin.user.login.application.CustomSuccessHandler;
import com.cleanengine.coin.user.login.application.JWTFilter;
import com.cleanengine.coin.user.login.application.JWTUtil;
import com.cleanengine.coin.user.login.application.CustomOAuth2UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;

import java.net.URI;
import java.util.Collections;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomSuccessHandler customSuccessHandler;
    private final JWTUtil jwtUtil;
    private final EndpointConfig endpointConfig;

    @Value("${frontend.url}")
    private String frontendUrl;

    public SecurityConfig(CustomOAuth2UserService customOAuth2UserService,
                          CustomSuccessHandler customSuccessHandler, JWTUtil jwtUtil, EndpointConfig endpointConfig) {
        this.customOAuth2UserService = customOAuth2UserService;
        this.customSuccessHandler = customSuccessHandler;
        this.jwtUtil = jwtUtil;
        this.endpointConfig = endpointConfig;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        String frontendBaseUrl = buildBaseUrl(new URI(frontendUrl));

        http
                .cors(corsCustomizer -> corsCustomizer.configurationSource(request -> {
                    CorsConfiguration configuration = new CorsConfiguration();

                    configuration.setAllowedOrigins(List.of(frontendBaseUrl,"http://localhost:63343"));
                    configuration.setAllowedMethods(Collections.singletonList("*"));
                    configuration.setAllowCredentials(true);
                    configuration.setAllowedHeaders(Collections.singletonList("*"));
                    configuration.setMaxAge(3600L);
                    configuration.setExposedHeaders(List.of("Set-Cookie", "access_token"));

                    return configuration;
                }))
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .addFilterBefore(new JWTFilter(jwtUtil, endpointConfig), OAuth2LoginAuthenticationFilter.class)
                .oauth2Login(oauth -> oauth
                                .userInfoEndpoint(userInfoEndpointConfig -> userInfoEndpointConfig
                                        .userService(customOAuth2UserService))
                                .successHandler(customSuccessHandler)
//                        .failureHandler(customFailureHandler) // TODO 로그인 실패 처리
                                .authorizationEndpoint(endpoint -> endpoint
                                        .baseUri("/api/oauth2/authorization")
                                )
                                .redirectionEndpoint(endpoint -> endpoint
                                        .baseUri("/api/login/oauth2/code/*")
                                )
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(endpointConfig.getPublicPathPatterns()).permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()  // pre-flight 허용
                        .anyRequest().authenticated()
                )
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)) // H2 콘솔 접근용
        ;

        // TODO : OAuth 플랫폼 API의 access_token, refresh_token 관리

        return http.build();
    }

    private String buildBaseUrl(URI uri) {
        return String.format("%s://%s%s", 
            uri.getScheme(), 
            uri.getHost(), 
            uri.getPort() == -1 ? "" : ":" + uri.getPort()
        );
    }

}