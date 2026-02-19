package org.example.emmm.config;

import lombok.RequiredArgsConstructor;
import org.example.emmm.dto.RoomDto;
import org.example.emmm.service.PresenceService;
import org.example.emmm.service.RoomService;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.util.List;

@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final PresenceService presenceService;
    private final SimpMessagingTemplate template; // ⭐️ 웹소켓 발송기 추가
    private final RoomService roomService;

    // 1. 구독 이벤트 (유저가 방에 들어옴)
    @EventListener
    public void handleWebSocketSubscribeListener(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String destination = headerAccessor.getDestination();
        String sessionId = headerAccessor.getSessionId();

        if (destination != null && destination.startsWith("/topic/room/online/")) {
            Long roomId = Long.parseLong(destination.split("/")[4]);

            // 유저 ID 가져오기 (방어 로직 추가)
            if (headerAccessor.getUser() == null) return;
            Long userId = Long.parseLong(headerAccessor.getUser().getName());

            // 메모리에 접속 기록
            presenceService.enterRoom(roomId, userId, sessionId);

            List<RoomDto.RoomMemberResDto> wsRes = roomService.getRoomMembers(roomId);

            template.convertAndSend("/topic/room/online/" + roomId, wsRes);
        }
    }

    // 2. 연결 해제 이벤트 (유저가 나감)
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        PresenceService.RoomUserInfo leftUserInfo = presenceService.exitRoom(sessionId);

        if (leftUserInfo != null) {
            List<RoomDto.RoomMemberResDto> wsRes = roomService.getRoomMembers(leftUserInfo.roomId());
            template.convertAndSend(
                    "/topic/room/online/" + leftUserInfo.roomId(), wsRes
            );
        }
    }
}