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
            "AND ur.deleted = false " + // UserRoom 자체가 삭제되지 않았고
            "AND ur.user.deleted = false " + // 연결된 유저도 삭제되지 않았으며
            "AND ur.room.deleted = false") // 연결된 방도 삭제되지 않은 경우
    Optional<UserRoom> findActiveUserRoom(@Param("user") User user, @Param("room") Room room);

    @Query("SELECT ur FROM UserRoom ur " +
            "JOIN FETCH ur.user u " + // 멤버 목록을 가져올 때 유저 정보도 한 번에 가져오도록 성능 최적화
            "WHERE ur.room = :room " +
            "AND ur.deleted = false " +  // UserRoom 매핑 자체가 삭제되지 않았고
            "AND ur.room.deleted = false") // 해당 방(Room)도 삭제되지 않은 경우
    List<UserRoom> findAllActiveMembersByRoom(@Param("room") Room room);

    boolean existsByRoomAndUser(Room r, User u);

    @Query("SELECT ur FROM UserRoom ur " +
            "JOIN FETCH ur.room r " + // 유저의 방 목록을 보여줄 때 N+1 방지
            "WHERE ur.user.id = :userId " +
            "AND ur.deleted = false " +    // UserRoom(참여 정보)이 삭제되지 않았고
            "AND ur.user.deleted = false") // User(유저 자신)도 삭제되지 않은 경우
    List<UserRoom> findAllActiveByUserId(@Param("userId") Long userId);
}
