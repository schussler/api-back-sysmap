package com.sysmap.hubapi.service;

import com.sysmap.hubapi.repository.PreferenceRepository;
import com.sysmap.hubapi.repository.UserRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PreferenceRepository preferenceRepository;

    @InjectMocks
    private UserService userService;

    // TODO: shouldReturnAuthenticatedUser
    // TODO: shouldUpdateUserData
    // TODO: shouldUpdateAvatar
    // TODO: shouldThrowWhenPreferenceTypeIdIsInvalid
    // TODO: shouldDeactivateAccount
}
