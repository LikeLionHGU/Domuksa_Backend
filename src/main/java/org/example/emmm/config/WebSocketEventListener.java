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

        // ⭐ 1. 어떤 주소로 구독 요청이 왔는지 확인
        System.out.println("=== [웹소켓 SUBSCRIBE 감지] 목적지: " + destination + ", 세션 ID: " + sessionId);

        if (destination != null && destination.startsWith("/topic/room/online/")) {
            Long roomId = Long.parseLong(destination.split("/")[4]);

            // ⭐ 2. 유저 인증 정보 확인
            if (headerAccessor.getUser() == null) {
                System.out.println("=== [웹소켓 에러] 방 접속 실패: 유저 정보가 null 입니다! (JWT 인증 실패)");
                return;
            }
            Long userId = Long.parseLong(headerAccessor.getUser().getName());

            // ⭐ 3. 정상 입장 처리 시작 로그
            System.out.println("=== [웹소켓 입장 처리 중] 방 번호: " + roomId + ", 유저 ID: " + userId);

            // 메모리에 접속 기록
            presenceService.enterRoom(roomId, userId, sessionId);

            // 최신 전체 명단 조회
            List<RoomDto.RoomMemberResDto> wsRes = roomService.getRoomMembers(roomId);

            // 프론트엔드로 리스트 발송
            template.convertAndSend("/topic/room/online/" + roomId, wsRes);

            // ⭐ 4. 발송 완료 로그 (현재 방 인원수 확인)
            System.out.println("=== [웹소켓 입장 알림 발송 완료] 발송된 현재 명단 인원 수: " + wsRes.size() + "명");
        }
    }

    // 2. 연결 해제 이벤트 (유저가 나감)
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        // ⭐ 1. 소켓 끊어짐 감지 로그
        System.out.println("=== [웹소켓 DISCONNECT 감지] 세션 ID 끊어짐: " + sessionId);

        PresenceService.RoomUserInfo leftUserInfo = presenceService.exitRoom(sessionId);

        if (leftUserInfo != null) {
            // ⭐ 2. 메모리에서 정상적으로 지워졌을 때 로그
            System.out.println("=== [웹소켓 퇴장 처리 완료] 방 번호: " + leftUserInfo.roomId() + ", 유저 ID: " + leftUserInfo.userId());

            List<RoomDto.RoomMemberResDto> wsRes = roomService.getRoomMembers(leftUserInfo.roomId());
            template.convertAndSend(
                    "/topic/room/online/" + leftUserInfo.roomId(), wsRes
            );
        } else {
            // ⭐ 3. 방에 들어간 적이 없거나 이미 지워진 경우 로그
            System.out.println("=== [웹소켓 퇴장 무시] 방 접속 기록이 없는 세션입니다.");
        }
    }
}