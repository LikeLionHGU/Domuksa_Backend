package org.example.emmm.repository;

import org.example.emmm.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByGoogleSub(String sub);
    Optional<User> findByEmail(String email);
}
