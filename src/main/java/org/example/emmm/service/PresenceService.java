package org.example.emmm.service;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PresenceService {

    private final Map<Long, Set<Long>> roomOnlineUsers = new ConcurrentHashMap<>();
    private final Map<String, Long> sessionRoomMap = new ConcurrentHashMap<>();
    private final Map<String, Long> sessionUserMap = new ConcurrentHashMap<>();

    // ⭐️ 반환용 Record 생성 (어느 방에서 누가 나갔는지 담는 바구니)
    public record RoomUserInfo(Long roomId, Long userId) {}

    public void enterRoom(Long roomId, Long userId, String sessionId) {
        roomOnlineUsers.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet()).add(userId);
        sessionRoomMap.put(sessionId, roomId);
        sessionUserMap.put(sessionId, userId);
    }

    // ⭐️ 반환 타입 변경: 나간 사람의 정보를 리턴합니다.
    public RoomUserInfo exitRoom(String sessionId) {
        Long roomId = sessionRoomMap.remove(sessionId);
        Long userId = sessionUserMap.remove(sessionId);

        if (roomId != null && userId != null) {
            Set<Long> users = roomOnlineUsers.get(roomId);
            if (users != null) {
                users.remove(userId);
                if (users.isEmpty()) {
                    roomOnlineUsers.remove(roomId);
                }
            }
            return new RoomUserInfo(roomId, userId); // 나간 정보 반환!
        }
        return null; // 연결된 정보가 없으면 null
    }

    public boolean isUserOnline(Long roomId, Long userId) {
        Set<Long> users = roomOnlineUsers.get(roomId);
        return users != null && users.contains(userId);
    }
}