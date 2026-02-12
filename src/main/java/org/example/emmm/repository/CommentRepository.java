package org.example.emmm.repository;

import org.example.emmm.domain.Agenda;
import org.example.emmm.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    Optional<Comment> findByIdAndDeletedFalse(Long commentId);

    @Query("SELECT c FROM Comment c " +
            "WHERE c.agenda.id = :agendaId " +
            "AND c.deleted = false " +
            "AND c.agenda.deleted = false")
    List<Comment> findAllActiveByAgendaId(@Param("agendaId") Long agendaId);

    @Query("SELECT c FROM Comment c " +
            "WHERE c.agenda = :agenda " +
            "AND c.deleted = false " +
            "AND c.agenda.deleted = false")
    Optional<Comment> findActiveByAgenda(@Param("agenda") Agenda agenda);
}
