package org.example.emmm.config;

import lombok.RequiredArgsConstructor;
import org.example.emmm.service.PresenceService;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final PresenceService presenceService;

    // 1. 구독 이벤트 (유저가 방에 들어옴)
    @EventListener
    public void handleWebSocketSubscribeListener(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        
        String destination = headerAccessor.getDestination(); // 예: /topic/rooms/1
        String sessionId = headerAccessor.getSessionId();
        
        // 유저 정보 가져오기 (인증된 유저라고 가정)
        // Spring Security를 쓰고 있다면 Principal에서 가져옵니다.
        // Long userId = Long.parseLong(headerAccessor.getUser().getName()); 
        // *여기서는 헤더에 userId를 넣어서 보냈다고 가정하거나 Principal 사용*
        
        if (destination != null && destination.startsWith("/topic/rooms/")) {
            Long roomId = Long.parseLong(destination.split("/")[3]);
            
            // ⭐ 여기서 유저 ID를 어떻게 가져올지는 인증 방식에 따라 다름
            // 예시: Long userId = (Long) headerAccessor.getSessionAttributes().get("userId");
            // 일단 임시로 1L이라고 가정하겠습니다. 실제로는 Principal에서 꺼내야 합니다.
             Long userId = Long.parseLong(headerAccessor.getUser().getName());

            presenceService.enterRoom(roomId, userId, sessionId);
            
            // (선택) 여기에 "누가 들어왔다"는 메시지를 브로드캐스팅 할 수도 있음
        }
    }

    // 2. 연결 해제 이벤트 (유저가 나감)
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        presenceService.exitRoom(sessionId);
    }
}