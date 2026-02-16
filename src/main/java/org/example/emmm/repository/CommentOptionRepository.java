package org.example.emmm.repository;

import org.example.emmm.domain.CommentOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommentOptionRepository extends JpaRepository<CommentOption, Long> {

    Optional<CommentOption> findByIdAndDeletedFalse(Long commentOptionId);

    @Query("SELECT co FROM CommentOption co " +
            "JOIN FETCH co.comment c " +
            "WHERE c.id IN :commentIds " +
            "AND co.deleted = false " +
            "AND c.deleted = false")
    List<CommentOption> findAllActiveByCommentIdIn(@Param("commentIds") List<Long> commentIds);

    @Query("SELECT co FROM CommentOption co " +
            "WHERE co.comment.id = :commentId " +
            "AND co.deleted = false " +
            "AND co.comment.deleted = false")
    List<CommentOption> findAllActiveByCommentId(@Param("commentId") Long commentId);
}
