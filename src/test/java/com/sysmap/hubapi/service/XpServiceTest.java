package com.sysmap.hubapi.service;

import com.sysmap.hubapi.entity.Achievement;
import com.sysmap.hubapi.entity.User;
import com.sysmap.hubapi.repository.AchievementRepository;
import com.sysmap.hubapi.repository.UserAchievementRepository;
import com.sysmap.hubapi.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class XpServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private AchievementRepository achievementRepository;
    @Mock private UserAchievementRepository userAchievementRepository;

    @InjectMocks private XpService xpService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(xpService, "xpPerCheckinParticipant", 50);
        ReflectionTestUtils.setField(xpService, "xpPerCheckinCreator", 30);
        ReflectionTestUtils.setField(xpService, "xpPerLevel", 200);
    }

    @Test
    void shouldGrantXpToParticipantAndCreatorOnCheckIn() {
        User participant = buildUser(0, 1);
        User creator = buildUser(0, 1);

        xpService.grantCheckInXp(participant, creator);

        assertThat(participant.getXp()).isEqualTo(50);
        assertThat(creator.getXp()).isEqualTo(30);
        verify(userRepository, times(2)).save(any(User.class));
    }

    @Test
    void shouldLevelUpWhenXpThresholdReached() {
        User participant = buildUser(190, 1);
        User creator = buildUser(0, 1);

        xpService.grantCheckInXp(participant, creator);

        // 190 + 50 = 240 → level 2
        assertThat(participant.getLevel()).isEqualTo(2);
    }

    @Test
    void shouldNotGrantDuplicateAchievement() {
        User user = buildUser(900, 4); // level up para 5 acontecerá
        User creator = buildUser(0, 1);

        Achievement veteran = Achievement.builder()
                .id(UUID.randomUUID()).name("Veterano").criterion("Alcance o nível 5").build();
        when(achievementRepository.findByName("Veterano")).thenReturn(Optional.of(veteran));
        when(userAchievementRepository.existsByUser_IdAndAchievement_Id(user.getId(), veteran.getId()))
                .thenReturn(true); // já possui

        xpService.grantCheckInXp(user, creator);

        // não deve salvar achievement duplicado
        verify(userAchievementRepository, never()).save(any());
    }

    @Test
    void shouldGrantVeteranAchievementOnLevel5() {
        User user = buildUser(800, 4); // 800 + 50 = 850 → ainda level 4... precisa de 200*4=800 → level 5
        // 800 + 50 = 850 / 200 + 1 = 5
        User creator = buildUser(0, 1);

        Achievement veteran = Achievement.builder()
                .id(UUID.randomUUID()).name("Veterano").criterion("Alcance o nível 5").build();
        when(achievementRepository.findByName("Veterano")).thenReturn(Optional.of(veteran));
        when(userAchievementRepository.existsByUser_IdAndAchievement_Id(user.getId(), veteran.getId()))
                .thenReturn(false);

        xpService.grantCheckInXp(user, creator);

        assertThat(user.getLevel()).isEqualTo(5);
        verify(userAchievementRepository).save(any());
    }

    private User buildUser(int xp, int level) {
        return User.builder()
                .id(UUID.randomUUID()).name("Usuário").email("u@email.com")
                .cpf("000.000.000-00").password("x").avatar("a")
                .xp(xp).level(level).build();
    }
}
