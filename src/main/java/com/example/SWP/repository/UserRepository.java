package com.example.SWP.repository;

import com.example.SWP.dto.response.UserResponse;
import com.example.SWP.entity.User;
import com.example.SWP.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByRole(Role role);
}