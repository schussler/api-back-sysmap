package com.sysmap.hubapi.repository;

import com.sysmap.hubapi.entity.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AchievementRepository extends JpaRepository<Achievement, UUID> {
    // TODO: findByName
}
