package org.example.emmm.repository;

import org.example.emmm.domain.CommentOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommentOptionRepository extends JpaRepository<CommentOption, Long> {
    List<CommentOption> findAllByCommentIdAndDeletedFalse(Long commentId);

    Optional<CommentOption> findByIdAndDeletedFalse(Long commentOptionId);

    List<CommentOption> findAllByCommentIdInAndDeletedFalse(List<Long> commentIds);

}
