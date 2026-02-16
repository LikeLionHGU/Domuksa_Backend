package org.example.emmm.repository;

import org.example.emmm.domain.Room;
import org.example.emmm.domain.User;
import org.example.emmm.domain.UserRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRoomRepository extends JpaRepository<UserRoom,Long> {
    @Query("SELECT ur FROM UserRoom ur " +
            "WHERE ur.user = :user AND ur.room = :room " +
            "AND ur.deleted = false " +
            "AND ur.user.deleted = false " +
            "AND ur.room.deleted = false")
    Optional<UserRoom> findActiveUserRoom(@Param("user") User user, @Param("room") Room room);

    @Query("SELECT ur FROM UserRoom ur " +
            "JOIN FETCH ur.user u " +
            "WHERE ur.room = :room " +
            "AND ur.deleted = false " +
            "AND ur.room.deleted = false")
    List<UserRoom> findAllActiveMembersByRoom(@Param("room") Room room);

    boolean existsByRoomAndUser(Room r, User u);

    @Query("SELECT ur FROM UserRoom ur " +
            "JOIN FETCH ur.room r " +
            "WHERE ur.user.id = :userId " +
            "AND ur.deleted = false " +
            "AND ur.user.deleted = false")
    List<UserRoom> findAllActiveByUserId(@Param("userId") Long userId);
}
