package com.sysmap.hubapi.service;

import com.sysmap.hubapi.dto.request.RegisterRequest;
import com.sysmap.hubapi.dto.request.SignInRequest;
import com.sysmap.hubapi.dto.response.SignInResponse;
import com.sysmap.hubapi.entity.User;
import com.sysmap.hubapi.exception.AccountDeactivatedException;
import com.sysmap.hubapi.exception.DuplicateUserException;
import com.sysmap.hubapi.exception.UserNotFoundException;
import com.sysmap.hubapi.exception.WrongPasswordException;
import com.sysmap.hubapi.repository.UserAchievementRepository;
import com.sysmap.hubapi.repository.UserRepository;
import com.sysmap.hubapi.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private UserAchievementRepository userAchievementRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtTokenProvider jwtTokenProvider;

    @InjectMocks private AuthService authService;

    // ── register ──────────────────────────────────────────────────────────

    @Test
    void shouldRegisterUserSuccessfully() {
        var request = new RegisterRequest("João", "joao@email.com", "123.456.789-00", "senha123");
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userRepository.existsByCpf(request.cpf())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("hashed");

        authService.register(request);

        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldThrowWhenEmailAlreadyExists() {
        var request = new RegisterRequest("João", "joao@email.com", "123.456.789-00", "senha123");
        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateUserException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenCpfAlreadyExists() {
        var request = new RegisterRequest("João", "joao@email.com", "123.456.789-00", "senha123");
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userRepository.existsByCpf(request.cpf())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateUserException.class);

        verify(userRepository, never()).save(any());
    }

    // ── signIn ────────────────────────────────────────────────────────────

    @Test
    void shouldSignInSuccessfully() {
        var request = new SignInRequest("joao@email.com", "senha123");
        var user = activeUser();
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), user.getPassword())).thenReturn(true);
        when(jwtTokenProvider.generateToken(user)).thenReturn("jwt-token");
        when(userAchievementRepository.findByUserId(user.getId())).thenReturn(List.of());

        SignInResponse response = authService.signIn(request);

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.email()).isEqualTo(user.getEmail());
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        var request = new SignInRequest("naoexiste@email.com", "senha123");
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.signIn(request))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void shouldThrowWhenPasswordIsWrong() {
        var request = new SignInRequest("joao@email.com", "errada");
        var user = activeUser();
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), user.getPassword())).thenReturn(false);

        assertThatThrownBy(() -> authService.signIn(request))
                .isInstanceOf(WrongPasswordException.class);
    }

    @Test
    void shouldThrowWhenAccountIsDeactivated() {
        var request = new SignInRequest("joao@email.com", "senha123");
        var user = activeUser();
        user.setDeletedAt(LocalDateTime.now());
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.signIn(request))
                .isInstanceOf(AccountDeactivatedException.class);
    }

    // ── helpers ───────────────────────────────────────────────────────────

    private User activeUser() {
        return User.builder()
                .id(UUID.randomUUID())
                .name("João")
                .email("joao@email.com")
                .cpf("123.456.789-00")
                .password("hashed")
                .avatar("default-avatar")
                .xp(0)
                .level(1)
                .build();
    }
}
