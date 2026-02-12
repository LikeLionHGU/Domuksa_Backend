package org.example.emmm.repository;

import org.example.emmm.domain.VoteSelection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VoteSelectionRepository extends JpaRepository<VoteSelection, Long> {
    @Query("SELECT vs FROM VoteSelection vs " +
            "WHERE vs.user.id = :userId " +
            "AND vs.vote.id = :voteId " +
            "AND vs.deleted = false " +       // 투표 기록 자체가 삭제되지 않았고
            "AND vs.user.deleted = false " +  // 투표한 유저도 삭제되지 않았으며
            "AND vs.vote.deleted = false")    // 해당 투표 자체도 삭제되지 않은 경우
    Optional<VoteSelection> findActiveVoteSelection(
            @Param("userId") Long userId,
            @Param("voteId") Long voteId
    );

    int countByVoteOptionIdAndDeletedFalse(Long id);

    List<VoteSelection> findAllByVoteIdAndDeletedFalse(Long voteId);
}
