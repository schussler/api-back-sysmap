package com.sysmap.hubapi.service;

import com.sysmap.hubapi.repository.ActivityParticipantRepository;
import com.sysmap.hubapi.repository.ActivityRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ParticipantServiceTest {

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private ActivityParticipantRepository participantRepository;

    @InjectMocks
    private ParticipantService participantService;

    // TODO: shouldSubscribeToPublicActivityWithAutoApproval
    // TODO: shouldSubscribeToPrivateActivityAsPending
    // TODO: shouldThrowWhenCreatorTriesToSubscribe
    // TODO: shouldCheckInSuccessfully
    // TODO: shouldThrowWhenConfirmationCodeIsWrong
    // TODO: shouldThrowWhenAlreadyCheckedIn
    // TODO: shouldThrowWhenUnsubscribingAfterCheckIn
}
