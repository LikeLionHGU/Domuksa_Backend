package org.example.emmm.repository;

import org.example.emmm.domain.Agenda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AgendaRepository extends JpaRepository<Agenda, Long> {
    Optional<Agenda> findByIdAndDeletedFalse(Long agendaId);

    @Query("SELECT a FROM Agenda a " +
            "JOIN FETCH a.config " +
            "WHERE a.room.id = :roomId " +
            "AND a.deleted = false " +
            "AND a.room.deleted = false")
    List<Agenda> findAllActiveByRoomId(@Param("roomId") Long roomId);

    @Query("SELECT MAX(a.sequence) FROM Agenda a WHERE a.room.id = :roomId AND a.deleted = false")
    Integer findMaxSequenceByRoomId(@Param("roomId") Long roomId);

    @Modifying
    @Query("UPDATE Agenda a SET a.sequence = a.sequence - 1 " +
            "WHERE a.room.id = :roomId " +
            "AND a.sequence > :deletedSequence " +
            "AND a.deleted = false")
    void decreaseSequenceAbove(@Param("roomId") Long roomId, @Param("deletedSequence") int deletedSequence);

    boolean existsByIdAndDeletedFalse(Long agendaId);
}
