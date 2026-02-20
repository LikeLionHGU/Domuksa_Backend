package org.example.emmm.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        // JavaTimeModule 등 클래스패스에 있는 모듈 자동 등록
        return new ObjectMapper().findAndRegisterModules();
    }
}
