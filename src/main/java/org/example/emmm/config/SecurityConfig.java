package org.example.emmm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // ✅ 개발용: 전부 허용
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                )

                // ✅ Swagger에서 POST/PUT/DELETE 테스트 편하게 하려면 CSRF 끄는 게 제일 간단
                .csrf(csrf -> csrf.disable())

                // ✅ Basic 로그인 팝업/기본 로그인 폼 자체를 막음
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(form -> form.disable());

        return http.build();
    }
}
