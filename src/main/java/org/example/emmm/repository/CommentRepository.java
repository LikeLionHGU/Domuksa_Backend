package org.example.emmm.repository;

import org.example.emmm.domain.Agenda;
import org.example.emmm.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    Optional<Comment> findByAgendaAndDeletedFalse(Agenda a);

    Optional<Comment> findByIdAndDeletedFalse(Long commentId);

    List<Comment> findAllByAgendaIdAndDeletedFalse(Long agendaId);
}
