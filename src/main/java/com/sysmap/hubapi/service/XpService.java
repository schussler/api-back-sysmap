package com.sysmap.hubapi.service;

import com.sysmap.hubapi.entity.User;
import com.sysmap.hubapi.entity.UserAchievement;
import com.sysmap.hubapi.repository.AchievementRepository;
import com.sysmap.hubapi.repository.UserAchievementRepository;
import com.sysmap.hubapi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class XpService {

    @Value("${xp.per-checkin-participant}")
    private int xpPerCheckinParticipant;

    @Value("${xp.per-checkin-creator}")
    private int xpPerCheckinCreator;

    @Value("${xp.per-level}")
    private int xpPerLevel;

    private final UserRepository userRepository;
    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;

    public XpService(UserRepository userRepository,
                     AchievementRepository achievementRepository,
                     UserAchievementRepository userAchievementRepository) {
        this.userRepository = userRepository;
        this.achievementRepository = achievementRepository;
        this.userAchievementRepository = userAchievementRepository;
    }

    public void grantCheckInXp(User participant, User creator) {
        participant.setXp(participant.getXp() + xpPerCheckinParticipant);
        checkLevelUp(participant);
        userRepository.save(participant);

        creator.setXp(creator.getXp() + xpPerCheckinCreator);
        checkLevelUp(creator);
        userRepository.save(creator);
    }

    public void checkFirstCheckin(User user) {
        grantAchievement(user, "Primeiro Passo");
    }

    // ── helpers ───────────────────────────────────────────────────────────

    private void checkLevelUp(User user) {
        int newLevel = user.getXp() / xpPerLevel + 1;
        if (newLevel > user.getLevel()) {
            user.setLevel(newLevel);
            checkLevelAchievement(user);
        }
    }

    private void checkLevelAchievement(User user) {
        if (user.getLevel() >= 5) {
            grantAchievement(user, "Veterano");
        }
    }

    private void grantAchievement(User user, String achievementName) {
        achievementRepository.findByName(achievementName).ifPresent(achievement -> {
            boolean alreadyGranted = userAchievementRepository
                    .existsByUser_IdAndAchievement_Id(user.getId(), achievement.getId());
            if (!alreadyGranted) {
                userAchievementRepository.save(
                        UserAchievement.builder()
                                .user(user)
                                .achievement(achievement)
                                .build());
            }
        });
    }
}
