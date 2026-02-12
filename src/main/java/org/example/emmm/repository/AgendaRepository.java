package org.example.emmm.repository;

import org.example.emmm.domain.Agenda;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AgendaRepository extends JpaRepository<Agenda, Long> {
    Optional<Agenda> findByIdAndDeletedFalse(Long agendaId);

    List<Agenda> findByRoomIdAndDeletedFalse(Long roomId);
}
