package com.facebook.UserService.repository;

import com.facebook.UserService.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, java.util.UUID> {
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    java.util.Optional<User> findByUsername(String username);
}
