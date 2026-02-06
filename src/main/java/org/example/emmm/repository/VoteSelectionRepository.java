package org.example.emmm.repository;

import org.example.emmm.domain.VoteSelection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VoteSelectionRepository extends JpaRepository<VoteSelection, Long> {
    Optional<VoteSelection> findByUserIdAndVoteIdAndDeletedFalse(Long userId, Long voteId);
}
