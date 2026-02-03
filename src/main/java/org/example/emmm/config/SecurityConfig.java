package org.example.emmm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            // 템플릿 테스트용: 로그인 API/페이지/스웨거는 열어둠
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/", "/login",
                    "/user/google", "/user/me", "/user/logout",
                    "/swagger-ui.html", "/swagger-ui/**",
                    "/v3/api-docs/**"
                ).permitAll()
                .anyRequest().authenticated()
            )

            // 브라우저 fetch로 POST 테스트할 거라서 /user/** 는 CSRF 제외 (개발용)
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/user/**", "/v3/api-docs/**", "/swagger-ui/**")
            )

            // 일단 기본 로그인 폼은 비활성화해도 되고 켜둬도 됨
            .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}
