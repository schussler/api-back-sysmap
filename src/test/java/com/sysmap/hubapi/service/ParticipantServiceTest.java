package com.sysmap.hubapi.service;

import com.sysmap.hubapi.dto.request.CheckInRequest;
import com.sysmap.hubapi.dto.response.SubscriptionResponse;
import com.sysmap.hubapi.entity.*;
import com.sysmap.hubapi.exception.*;
import com.sysmap.hubapi.repository.ActivityParticipantRepository;
import com.sysmap.hubapi.repository.ActivityRepository;
import com.sysmap.hubapi.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
class ParticipantServiceTest {

    @Mock private ActivityRepository activityRepository;
    @Mock private ActivityParticipantRepository participantRepository;
    @Mock private UserRepository userRepository;
    @Mock private XpService xpService;

    @InjectMocks private ParticipantService participantService;

    private User user;
    private User creator;
    private Activity publicActivity;
    private Activity privateActivity;

    @BeforeEach
    void setUp() {
        creator = User.builder().id(UUID.randomUUID()).name("Criador").build();
        user = User.builder().id(UUID.randomUUID()).name("Participante")
                .email("p@email.com").xp(0).level(1).build();

        ActivityType type = ActivityType.builder().id(UUID.randomUUID()).name("Esporte").description("x").build();

        publicActivity = Activity.builder()
                .id(UUID.randomUUID()).title("Corrida").description("5km")
                .type(type).confirmationCode("CODE01").image("img")
                .scheduledDate(LocalDateTime.now().plusDays(1))
                .createdAt(LocalDateTime.now()).isPrivate(false).creator(creator).build();

        privateActivity = Activity.builder()
                .id(UUID.randomUUID()).title("Yoga").description("Relaxante")
                .type(type).confirmationCode("YOGI01").image("img")
                .scheduledDate(LocalDateTime.now().plusDays(1))
                .createdAt(LocalDateTime.now()).isPrivate(true).creator(creator).build();

        var auth = new UsernamePasswordAuthenticationToken(
                user, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    // ── subscribe ─────────────────────────────────────────────────────────

    @Test
    void shouldSubscribeToPublicActivityWithAutoApproval() {
        when(activityRepository.findById(publicActivity.getId()))
                .thenReturn(Optional.of(publicActivity));
        when(participantRepository.existsByActivity_IdAndUser_Id(publicActivity.getId(), user.getId()))
                .thenReturn(false);
        when(participantRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SubscriptionResponse response = participantService.subscribe(publicActivity.getId());

        assertThat(response.approved()).isTrue();
        verify(participantRepository).save(any(ActivityParticipant.class));
    }

    @Test
    void shouldSubscribeToPrivateActivityAsPending() {
        when(activityRepository.findById(privateActivity.getId()))
                .thenReturn(Optional.of(privateActivity));
        when(participantRepository.existsByActivity_IdAndUser_Id(privateActivity.getId(), user.getId()))
                .thenReturn(false);
        when(participantRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SubscriptionResponse response = participantService.subscribe(privateActivity.getId());

        assertThat(response.approved()).isFalse();
    }

    @Test
    void shouldThrowWhenCreatorTriesToSubscribe() {
        var auth = new UsernamePasswordAuthenticationToken(
                creator, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);
        when(activityRepository.findById(publicActivity.getId()))
                .thenReturn(Optional.of(publicActivity));

        assertThatThrownBy(() -> participantService.subscribe(publicActivity.getId()))
                .isInstanceOf(CreatorSubscribeException.class);
    }

    @Test
    void shouldThrowWhenAlreadySubscribed() {
        when(activityRepository.findById(publicActivity.getId()))
                .thenReturn(Optional.of(publicActivity));
        when(participantRepository.existsByActivity_IdAndUser_Id(publicActivity.getId(), user.getId()))
                .thenReturn(true);

        assertThatThrownBy(() -> participantService.subscribe(publicActivity.getId()))
                .isInstanceOf(AlreadySubscribedException.class);
    }

    // ── checkIn ───────────────────────────────────────────────────────────

    @Test
    void shouldCheckInSuccessfully() {
        ActivityParticipant ap = ActivityParticipant.builder()
                .id(UUID.randomUUID()).activity(publicActivity).user(user).approved(true).build();

        when(activityRepository.findById(publicActivity.getId()))
                .thenReturn(Optional.of(publicActivity));
        when(participantRepository.findByActivity_IdAndUser_Id(publicActivity.getId(), user.getId()))
                .thenReturn(Optional.of(ap));
        when(userRepository.findById(creator.getId())).thenReturn(Optional.of(creator));

        participantService.checkIn(publicActivity.getId(), new CheckInRequest("CODE01"));

        assertThat(ap.getConfirmedAt()).isNotNull();
        verify(xpService).grantCheckInXp(user, creator);
        verify(xpService).checkFirstCheckin(user);
    }

    @Test
    void shouldThrowWhenConfirmationCodeIsWrong() {
        ActivityParticipant ap = ActivityParticipant.builder()
                .id(UUID.randomUUID()).activity(publicActivity).user(user).approved(true).build();

        when(activityRepository.findById(publicActivity.getId()))
                .thenReturn(Optional.of(publicActivity));
        when(participantRepository.findByActivity_IdAndUser_Id(publicActivity.getId(), user.getId()))
                .thenReturn(Optional.of(ap));

        assertThatThrownBy(() -> participantService.checkIn(publicActivity.getId(), new CheckInRequest("WRONG1")))
                .isInstanceOf(WrongConfirmationCodeException.class);
    }

    @Test
    void shouldThrowWhenAlreadyCheckedIn() {
        ActivityParticipant ap = ActivityParticipant.builder()
                .id(UUID.randomUUID()).activity(publicActivity).user(user)
                .approved(true).confirmedAt(LocalDateTime.now()).build();

        when(activityRepository.findById(publicActivity.getId()))
                .thenReturn(Optional.of(publicActivity));
        when(participantRepository.findByActivity_IdAndUser_Id(publicActivity.getId(), user.getId()))
                .thenReturn(Optional.of(ap));

        assertThatThrownBy(() -> participantService.checkIn(publicActivity.getId(), new CheckInRequest("CODE01")))
                .isInstanceOf(AlreadyCheckedInException.class);
    }

    @Test
    void shouldThrowWhenUnsubscribingAfterCheckIn() {
        ActivityParticipant ap = ActivityParticipant.builder()
                .id(UUID.randomUUID()).activity(publicActivity).user(user)
                .approved(true).confirmedAt(LocalDateTime.now()).build();

        when(activityRepository.findById(publicActivity.getId()))
                .thenReturn(Optional.of(publicActivity));
        when(participantRepository.findByActivity_IdAndUser_Id(publicActivity.getId(), user.getId()))
                .thenReturn(Optional.of(ap));

        assertThatThrownBy(() -> participantService.unsubscribe(publicActivity.getId()))
                .isInstanceOf(CheckInDoneException.class);
    }
}
