package com.sysmap.hubapi.service;

import com.sysmap.hubapi.repository.AchievementRepository;
import com.sysmap.hubapi.repository.UserAchievementRepository;
import com.sysmap.hubapi.repository.UserRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class XpServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AchievementRepository achievementRepository;

    @Mock
    private UserAchievementRepository userAchievementRepository;

    @InjectMocks
    private XpService xpService;

    // TODO: shouldGrantXpToParticipantAndCreatorOnCheckIn
    // TODO: shouldLevelUpWhenXpThresholdReached
    // TODO: shouldNotGrantDuplicateAchievement
}
