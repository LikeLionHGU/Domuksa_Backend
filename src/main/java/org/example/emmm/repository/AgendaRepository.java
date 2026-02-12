package org.example.emmm.repository;

import org.example.emmm.domain.Agenda;
import org.springframework.data.jpa.repository.JpaRepository;
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
    List<Agenda> findAllActiveByRoomId(@Param("roomId") Long roomId);}
