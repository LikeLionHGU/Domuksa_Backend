package org.example.emmm.repository;

import org.example.emmm.domain.Room;
import org.example.emmm.domain.User;
import org.example.emmm.domain.UserRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRoomRepository extends JpaRepository<UserRoom,Long> {
    Optional<UserRoom> findByUserAndRoom(User user, Room room);
    List<UserRoom> findAllByRoom(Room room);

    List<UserRoom> findAllByUserId(Long id);
}
