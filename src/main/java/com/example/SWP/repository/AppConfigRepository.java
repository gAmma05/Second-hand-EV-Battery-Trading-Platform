package com.example.SWP.repository;

import com.example.SWP.entity.AppConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppConfigRepository extends JpaRepository<AppConfig, Long> {
    boolean existsByConfigKey(String configKey);
    Optional<AppConfig> findByConfigKey(String configKey);
}
