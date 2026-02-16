package org.example.emmm.service;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PresenceService {

    // RoomId -> 접속중인 UserId Set
    private final Map<Long, Set<Long>> roomOnlineUsers = new ConcurrentHashMap<>();
    
    // SessionId -> RoomId (연결 끊길 때 어떤 방에서 나갔는지 알기 위해 필요)
    private final Map<String, Long> sessionRoomMap = new ConcurrentHashMap<>();
    
    // SessionId -> UserId
    private final Map<String, Long> sessionUserMap = new ConcurrentHashMap<>();

    // 유저 입장 처리
    public void enterRoom(Long roomId, Long userId, String sessionId) {
        roomOnlineUsers.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet()).add(userId);
        sessionRoomMap.put(sessionId, roomId);
        sessionUserMap.put(sessionId, userId);
    }

    // 유저 퇴장 처리
    public void exitRoom(String sessionId) {
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
        }
    }

    // 특정 방에 특정 유저가 접속 중인지 확인
    public boolean isUserOnline(Long roomId, Long userId) {
        Set<Long> users = roomOnlineUsers.get(roomId);
        return users != null && users.contains(userId);
    }
}