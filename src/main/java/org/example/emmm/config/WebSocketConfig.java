package org.example.emmm.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean; // 추가
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler; // 추가
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler; // 추가
import org.springframework.web.socket.config.annotation.*;

@Configuration
@RequiredArgsConstructor
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final StompHandler stompHandler;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins("http://localhost:3000")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic")
                // ⭐ 서버가 10초마다 확인, 10초 내 응답 없으면 연결 끊음
                .setHeartbeatValue(new long[]{10000, 10000})
                .setTaskScheduler(heartbeatScheduler()); // 스케줄러 연결

        registry.setApplicationDestinationPrefixes("/app");
    }

    // ⭐ Heartbeat를 관리할 스케줄러 빈 등록
    @Bean
    public TaskScheduler heartbeatScheduler() {
        return new ThreadPoolTaskScheduler();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompHandler);
    }
}