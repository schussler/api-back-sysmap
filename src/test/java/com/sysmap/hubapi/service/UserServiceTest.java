package com.sysmap.hubapi.service;

import com.sysmap.hubapi.dto.request.UpdateUserRequest;
import com.sysmap.hubapi.dto.response.UserResponse;
import com.sysmap.hubapi.entity.ActivityType;
import com.sysmap.hubapi.entity.User;
import com.sysmap.hubapi.exception.DuplicateUserException;
import com.sysmap.hubapi.exception.MissingFieldsException;
import com.sysmap.hubapi.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PreferenceRepository preferenceRepository;
    @Mock private ActivityTypeRepository activityTypeRepository;
    @Mock private AchievementRepository achievementRepository;
    @Mock private UserAchievementRepository userAchievementRepository;
    @Mock private S3Service s3Service;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(UUID.randomUUID())
                .name("Maria")
                .email("maria@email.com")
                .cpf("111.222.333-44")
                .password("hashed")
                .avatar("default-avatar")
                .xp(0)
                .level(1)
                .build();

        var auth = new UsernamePasswordAuthenticationToken(
                user, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void shouldReturnAuthenticatedUser() {
        when(userAchievementRepository.findByUserId(user.getId())).thenReturn(List.of());

        UserResponse response = userService.getAuthenticatedUser();

        assertThat(response.email()).isEqualTo(user.getEmail());
        assertThat(response.achievements()).isEmpty();
    }

    @Test
    void shouldReturnUserPreferences() {
        when(preferenceRepository.findByUserId(user.getId())).thenReturn(List.of());

        var prefs = userService.getPreferences();

        assertThat(prefs).isEmpty();
    }

    @Test
    void shouldThrowWhenPreferenceTypeIdIsInvalid() {
        List<UUID> typeIds = List.of(UUID.randomUUID(), UUID.randomUUID());
        when(activityTypeRepository.findAllById(typeIds)).thenReturn(List.of());

        assertThatThrownBy(() -> userService.definePreferences(typeIds))
                .isInstanceOf(MissingFieldsException.class);
    }

    @Test
    void shouldDefinePreferencesSuccessfully() {
        var type = ActivityType.builder()
                .id(UUID.randomUUID()).name("Esporte").description("Esporte").build();
        List<UUID> typeIds = List.of(type.getId());
        when(activityTypeRepository.findAllById(typeIds)).thenReturn(List.of(type));

        userService.definePreferences(typeIds);

        verify(preferenceRepository).deleteByUser(user);
        verify(preferenceRepository).saveAll(any());
    }

    @Test
    void shouldUpdateUserName() {
        when(userAchievementRepository.findByUserId(user.getId())).thenReturn(List.of());

        UserResponse response = userService.updateUser(new UpdateUserRequest("Novo Nome", null, null));

        assertThat(response.name()).isEqualTo("Novo Nome");
        verify(userRepository).save(user);
    }

    @Test
    void shouldThrowWhenEmailAlreadyTaken() {
        when(userRepository.existsByEmail("outro@email.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.updateUser(new UpdateUserRequest(null, "outro@email.com", null)))
                .isInstanceOf(DuplicateUserException.class);
    }

    @Test
    void shouldDeactivateAccount() {
        userService.deactivate();

        assertThat(user.getDeletedAt()).isNotNull();
        verify(userRepository).save(user);
    }
}
