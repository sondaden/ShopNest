// src/main/java/com/sonnhuynhh/shopnest/repository/UserRepository.java
package com.sonnhuynhh.shopnest.repository;

import com.sonnhuynhh.shopnest.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}