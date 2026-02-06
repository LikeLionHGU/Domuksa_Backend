package org.example.emmm.repository;

import org.example.emmm.domain.Vote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VoteRepository extends JpaRepository<Vote, Long> {
    Optional<Vote> findByAgendaIdAndDeletedFalse(Long agendaId);

    Optional<Vote> findByIdAndDeletedFalse(Long voteId);
}
