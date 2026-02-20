package org.example.emmm.config;

import lombok.RequiredArgsConstructor;
import org.example.emmm.security.AuthService;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.security.Principal;


@Component
@RequiredArgsConstructor
public class StompHandler implements ChannelInterceptor {
    private final AuthService authService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String jwt = accessor.getFirstNativeHeader("Authorization");

            if (jwt != null && jwt.startsWith("Bearer ")) {
                try {
                    String token = jwt.substring(7);
                    Long userId = authService.verifyAccessToken(token);

                    Principal principal = () -> String.valueOf(userId);
                    accessor.setUser(principal);
                    System.out.println("=== [StompHandler] 인증 성공: " + userId);
                } catch (Exception e) {
                    System.out.println("=== [StompHandler] 인증 실패: " + e.getMessage());
                    throw new IllegalArgumentException("인증 정보가 유효하지 않습니다.");
                }
            }
        }
        return message;
    }
}