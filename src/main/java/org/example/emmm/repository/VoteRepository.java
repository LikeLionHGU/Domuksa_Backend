package org.example.emmm.repository;

import org.example.emmm.domain.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface VoteRepository extends JpaRepository<Vote, Long> {

    @Query("SELECT v FROM Vote v " +
            "WHERE v.agenda.id = :agendaId " +
            "AND v.deleted = false " +    // 투표 자체가 삭제되지 않았고
            "AND v.agenda.deleted = false") // 연결된 안건도 삭제되지 않은 경우
    Optional<Vote> findActiveByAgendaId(@Param("agendaId") Long agendaId);

    Optional<Vote> findByIdAndDeletedFalse(Long voteId);
}
