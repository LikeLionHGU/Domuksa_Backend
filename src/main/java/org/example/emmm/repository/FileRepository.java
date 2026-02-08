package org.example.emmm.repository;

import org.example.emmm.domain.File;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FileRepository extends JpaRepository<File, Long> {
List<File> findByAgendaId(Long agendaId);
}