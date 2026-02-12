package org.example.emmm.repository;

import org.example.emmm.domain.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VoteRepository extends JpaRepository<Vote, Long> {

    @Query("SELECT v FROM Vote v " +
            "WHERE v.agenda.id = :agendaId " +
            "AND v.deleted = false " +
            "AND v.agenda.deleted = false")
    List<Vote> findAllActiveByAgendaId(@Param("agendaId") Long agendaId);

    @Query("SELECT v FROM Vote v " +
            "WHERE v.agenda.id = :agendaId " +
            "AND v.deleted = false " +
            "AND v.agenda.deleted = false")
    Optional<Vote> findActiveByAgendaId(@Param("agendaId") Long agendaId);

    Optional<Vote> findByIdAndDeletedFalse(Long voteId);
}
