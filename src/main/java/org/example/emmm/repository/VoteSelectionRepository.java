package org.example.emmm.repository;

import org.example.emmm.domain.VoteSelection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VoteSelectionRepository extends JpaRepository<VoteSelection, Long> {
    @Query(value = "SELECT vs FROM VoteSelection vs " +
            "WHERE vs.user.id = :userId " +
            "AND vs.vote.id = :voteId " +
            "AND vs.deleted = false " +
            "AND vs.user.deleted = false " +
            "AND vs.vote.deleted = false " +
            "ORDER BY vs.createdAt DESC LIMIT 1") // ✅ 가장 최근 데이터 1개만 선별
    Optional<VoteSelection> findActiveVoteSelection(
            @Param("userId") Long userId,
            @Param("voteId") Long voteId
    );

    int countByVoteOptionIdAndDeletedFalse(Long id);

    List<VoteSelection> findAllByVoteIdAndDeletedFalse(Long voteId);
}
