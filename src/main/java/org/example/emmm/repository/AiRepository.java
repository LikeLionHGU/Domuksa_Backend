package org.example.emmm.repository;

import org.example.emmm.domain.Ai;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AiRepository extends JpaRepository<Ai, Long> {
    @Query("SELECT ai FROM Ai ai " +
            "WHERE ai.agenda.id = :agendaId " +
            "AND ai.deleted = false " +
            "AND ai.agenda.deleted = false")
    Optional<Ai> findActiveAiByAgendaId(@Param("agendaId") Long agendaId);
}
