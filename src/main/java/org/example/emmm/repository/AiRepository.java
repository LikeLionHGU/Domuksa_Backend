package org.example.emmm.repository;

import org.example.emmm.domain.Ai;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AiRepository extends JpaRepository<Ai, Long> {
    Optional<Ai> findByAgendaIdAndDeletedFalse(Long agendaId);

}
