package org.example.emmm.repository;

import org.example.emmm.domain.VoteOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VoteOptionRepository extends JpaRepository<VoteOption, Long> {
    @Query("SELECT vo FROM VoteOption vo " +
            "WHERE vo.vote.id = :voteId " +
            "AND vo.deleted = false " +
            "AND vo.vote.deleted = false")
    List<VoteOption> findAllActiveByVoteId(@Param("voteId") Long voteId);

    Optional<VoteOption> findByIdAndDeletedFalse(Long voteOptionId);
}
