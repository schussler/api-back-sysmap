package com.sysmap.hubapi.repository;

import com.sysmap.hubapi.entity.UserAchievement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserAchievementRepository extends JpaRepository<UserAchievement, UUID> {
    // TODO: existsByUserIdAndAchievementId, findByUserId
}
