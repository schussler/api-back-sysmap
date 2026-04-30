package com.sysmap.hubapi.service;

import com.sysmap.hubapi.dto.response.ActivityResponse;
import com.sysmap.hubapi.dto.response.PagedActivityResponse;
import com.sysmap.hubapi.entity.*;
import com.sysmap.hubapi.exception.ActivityNotFoundException;
import com.sysmap.hubapi.exception.NotActivityCreatorException;
import com.sysmap.hubapi.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActivityServiceTest {

    @Mock private ActivityRepository activityRepository;
    @Mock private ActivityTypeRepository activityTypeRepository;
    @Mock private ActivityAddressRepository activityAddressRepository;
    @Mock private ActivityParticipantRepository activityParticipantRepository;
    @Mock private PreferenceRepository preferenceRepository;
    @Mock private AchievementRepository achievementRepository;
    @Mock private UserAchievementRepository userAchievementRepository;
    @Mock private S3Service s3Service;

    @InjectMocks private ActivityService activityService;

    private User user;
    private ActivityType type;
    private Activity activity;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(UUID.randomUUID()).name("Ana").email("ana@email.com")
                .cpf("000.000.000-00").password("hashed").avatar("default-avatar")
                .xp(0).level(1).build();

        type = ActivityType.builder()
                .id(UUID.randomUUID()).name("Esporte").description("Esportes").build();

        activity = Activity.builder()
                .id(UUID.randomUUID()).title("Corrida").description("5km")
                .type(type).confirmationCode("ABC123").image("http://img")
                .scheduledDate(LocalDateTime.now().plusDays(1))
                .createdAt(LocalDateTime.now()).isPrivate(false).creator(user)
                .build();

        var auth = new UsernamePasswordAuthenticationToken(
                user, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void shouldListActivitiesFilteredByType() {
        when(activityRepository.findByType_IdAndDeletedAtIsNullAndCompletedAtIsNull(
                eq(type.getId()), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(activity)));
        when(activityAddressRepository.findByActivity_Id(activity.getId())).thenReturn(Optional.empty());
        when(activityParticipantRepository.countByActivity_Id(activity.getId())).thenReturn(0L);

        PagedActivityResponse result = activityService.listActivities(1, 10, type.getId(), "createdAt", "desc");

        assertThat(result.activities()).hasSize(1);
        assertThat(result.activities().get(0).title()).isEqualTo("Corrida");
    }

    @Test
    void shouldListActivitiesPrioritizingUserPreferences() {
        when(preferenceRepository.findByUserId(user.getId())).thenReturn(List.of());
        when(activityRepository.findByDeletedAtIsNullAndCompletedAtIsNull(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(activity)));
        when(activityAddressRepository.findByActivity_Id(activity.getId())).thenReturn(Optional.empty());
        when(activityParticipantRepository.countByActivity_Id(activity.getId())).thenReturn(0L);

        PagedActivityResponse result = activityService.listActivities(1, 10, null, "createdAt", "desc");

        assertThat(result.activities()).hasSize(1);
    }

    @Test
    void shouldThrowWhenNonCreatorTriesToConclude() {
        User other = User.builder().id(UUID.randomUUID()).name("Outro").build();
        activity.setCreator(other);

        when(activityRepository.findById(activity.getId())).thenReturn(Optional.of(activity));

        assertThatThrownBy(() -> activityService.concludeActivity(activity.getId()))
                .isInstanceOf(NotActivityCreatorException.class);
    }

    @Test
    void shouldConcludeActivitySuccessfully() {
        when(activityRepository.findById(activity.getId())).thenReturn(Optional.of(activity));
        when(achievementRepository.findByName("Mestre de Cerimônias")).thenReturn(Optional.empty());

        activityService.concludeActivity(activity.getId());

        assertThat(activity.getCompletedAt()).isNotNull();
        verify(activityRepository).save(activity);
    }

    @Test
    void shouldDeleteActivitySuccessfully() {
        when(activityRepository.findById(activity.getId())).thenReturn(Optional.of(activity));

        activityService.deleteActivity(activity.getId());

        assertThat(activity.getDeletedAt()).isNotNull();
        verify(activityRepository).save(activity);
    }

    @Test
    void shouldThrowWhenActivityNotFound() {
        when(activityRepository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> activityService.concludeActivity(UUID.randomUUID()))
                .isInstanceOf(ActivityNotFoundException.class);
    }

    @Test
    void shouldExposeConfirmationCodeOnlyToCreator() {
        when(activityRepository.findById(activity.getId())).thenReturn(Optional.of(activity));
        when(activityAddressRepository.findByActivity_Id(activity.getId())).thenReturn(Optional.empty());
        when(activityParticipantRepository.countByActivity_Id(activity.getId())).thenReturn(0L);
        when(activityRepository.findByCreator_Id(eq(user.getId()), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(activity)));

        PagedActivityResponse result = activityService.listCreatedByUser(1, 10);
        ActivityResponse resp = result.activities().get(0);

        assertThat(resp.confirmationCode()).isEqualTo("ABC123");
    }
}
