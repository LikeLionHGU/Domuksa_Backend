package org.example.emmm.repository;

import org.example.emmm.domain.AgendaConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AgendaConfigRepository extends JpaRepository<AgendaConfig, Long> {
    Optional<AgendaConfig> findByIdAndDeletedFalse(Long id);
}

