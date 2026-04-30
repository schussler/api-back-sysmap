package com.sysmap.hubapi.repository;

import com.sysmap.hubapi.entity.UserAchievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface UserAchievementRepository extends JpaRepository<UserAchievement, UUID> {

    @Query("SELECT ua FROM UserAchievement ua JOIN FETCH ua.achievement WHERE ua.user.id = :userId")
    List<UserAchievement> findByUserId(@Param("userId") UUID userId);

    boolean existsByUser_IdAndAchievement_Id(UUID userId, UUID achievementId);
}
