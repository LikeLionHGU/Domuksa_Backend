package org.example.emmm.repository;

import org.example.emmm.domain.Timer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TimerRepository extends JpaRepository<Timer, Long> {
    Optional<Timer> findByRoomIdAndDeletedFalse(Long roomId);
}
