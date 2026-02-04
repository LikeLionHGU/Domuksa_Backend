package org.example.emmm.repository;

import org.example.emmm.domain.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {
    Optional<Room> findByCodeAndDeletedFalse(String code);

    Optional<Room> findByIdAndDeletedFalse(Long roomId);
}
