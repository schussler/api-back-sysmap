package com.sysmap.hubapi.service;

import com.sysmap.hubapi.dto.request.ApproveRequest;
import com.sysmap.hubapi.dto.request.CheckInRequest;
import com.sysmap.hubapi.dto.response.SubscriptionResponse;
import com.sysmap.hubapi.entity.Activity;
import com.sysmap.hubapi.entity.ActivityParticipant;
import com.sysmap.hubapi.entity.User;
import com.sysmap.hubapi.exception.*;
import com.sysmap.hubapi.repository.ActivityParticipantRepository;
import com.sysmap.hubapi.repository.ActivityRepository;
import com.sysmap.hubapi.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ParticipantService {

    private final ActivityRepository activityRepository;
    private final ActivityParticipantRepository participantRepository;
    private final UserRepository userRepository;
    private final XpService xpService;

    public ParticipantService(ActivityRepository activityRepository,
                              ActivityParticipantRepository participantRepository,
                              UserRepository userRepository,
                              XpService xpService) {
        this.activityRepository = activityRepository;
        this.participantRepository = participantRepository;
        this.userRepository = userRepository;
        this.xpService = xpService;
    }

    @Transactional
    public SubscriptionResponse subscribe(UUID activityId) {
        User user = currentUser();
        Activity activity = findActiveActivity(activityId);

        // E12: não pode estar concluída
        if (activity.getCompletedAt() != null) {
            throw new ActivityCompletedException("Esta atividade já foi concluída.");
        }

        // E8: criador não pode se inscrever
        if (activity.getCreator().getId().equals(user.getId())) {
            throw new CreatorSubscribeException();
        }

        // E7: já inscrito
        if (participantRepository.existsByActivity_IdAndUser_Id(activityId, user.getId())) {
            throw new AlreadySubscribedException();
        }

        boolean approved = !Boolean.TRUE.equals(activity.getIsPrivate());

        ActivityParticipant participant = ActivityParticipant.builder()
                .activity(activity)
                .user(user)
                .approved(approved)
                .build();
        participantRepository.save(participant);

        return new SubscriptionResponse(
                participant.getId(),
                activityId,
                user.getId(),
                approved,
                null);
    }

    @Transactional
    public void approve(UUID activityId, ApproveRequest request) {
        User user = currentUser();
        Activity activity = findActiveActivity(activityId);

        // E16: apenas o criador
        if (!activity.getCreator().getId().equals(user.getId())) {
            throw new NotActivityCreatorException("Apenas o criador da atividade pode aprovar inscrições.");
        }

        ActivityParticipant participant = participantRepository.findById(request.participantId())
                .filter(ap -> ap.getActivity().getId().equals(activityId))
                .orElseThrow(ParticipantNotFoundException::new);

        participant.setApproved(request.approved());
        participantRepository.save(participant);
    }

    @Transactional
    public void checkIn(UUID activityId, CheckInRequest request) {
        User user = currentUser();
        Activity activity = findActiveActivity(activityId);

        // E13: não pode estar concluída
        if (activity.getCompletedAt() != null) {
            throw new ActivityCompletedException("Não é possível fazer check-in em uma atividade concluída.");
        }

        // E9: deve ser participante aprovado
        ActivityParticipant participant = participantRepository
                .findByActivity_IdAndUser_Id(activityId, user.getId())
                .orElseThrow(NotApprovedParticipantException::new);

        if (!Boolean.TRUE.equals(participant.getApproved())) {
            throw new NotApprovedParticipantException();
        }

        // E11: não pode fazer check-in duas vezes
        if (participant.getConfirmedAt() != null) {
            throw new AlreadyCheckedInException();
        }

        // E10: código correto
        if (!activity.getConfirmationCode().equals(request.confirmationCode())) {
            throw new WrongConfirmationCodeException();
        }

        participant.setConfirmedAt(LocalDateTime.now());
        participantRepository.save(participant);

        // Concede XP ao participante e ao criador
        User creator = userRepository.findById(activity.getCreator().getId())
                .orElseThrow(UserNotFoundException::new);
        xpService.grantCheckInXp(user, creator);

        // Achievement: primeiro check-in
        xpService.checkFirstCheckin(user);
    }

    @Transactional
    public void unsubscribe(UUID activityId) {
        User user = currentUser();

        findActiveActivity(activityId); // garante que a atividade existe

        ActivityParticipant participant = participantRepository
                .findByActivity_IdAndUser_Id(activityId, user.getId())
                .orElseThrow(ActivityNotFoundException::new);

        // E18: não pode cancelar após check-in
        if (participant.getConfirmedAt() != null) {
            throw new CheckInDoneException();
        }

        participantRepository.delete(participant);
    }

    // ── helpers ───────────────────────────────────────────────────────────

    private User currentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private Activity findActiveActivity(UUID activityId) {
        return activityRepository.findById(activityId)
                .filter(a -> a.getDeletedAt() == null)
                .orElseThrow(ActivityNotFoundException::new);
    }
}
