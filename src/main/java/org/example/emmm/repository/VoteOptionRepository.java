package org.example.emmm.repository;

import org.example.emmm.domain.VoteOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VoteOptionRepository extends JpaRepository<VoteOption, Long> {
    List<VoteOption> findAllByVoteIdAndDeletedFalse(Long id);

    Optional<VoteOption> findByIdAndDeletedFalse(Long voteOptionId);

}
