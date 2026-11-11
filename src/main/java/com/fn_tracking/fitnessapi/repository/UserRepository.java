package com.fn_tracking.fitnessapi.repository;

import com.fn_tracking.fitnessapi.entity.Role;
import com.fn_tracking.fitnessapi.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Boolean existsByEmail(String email);

    Boolean existsByUsername(String username);

    Page<User> findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String usernameKeyword,
            String emailKeyword,
            Pageable pageable
    );

    long countByCreatedAtAfter(LocalDateTime time);

    long countByRole(Role role);
}
