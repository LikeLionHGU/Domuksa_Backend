package org.example.emmm.repository;

import org.example.emmm.domain.RoomDMMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoomDMMessageRepository extends JpaRepository<RoomDMMessage, Long> {
    List<RoomDMMessage> findByRoomId (Long roomId);
    List<RoomDMMessage> findByRoomIdAndUserRoomId(Long roomId,Long userRoomId);

}
