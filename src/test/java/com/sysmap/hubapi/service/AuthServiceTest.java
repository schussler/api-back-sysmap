package com.sysmap.hubapi.service;

import com.sysmap.hubapi.repository.UserRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    // TODO: shouldRegisterUserSuccessfully
    // TODO: shouldThrowWhenEmailAlreadyExists
    // TODO: shouldThrowWhenCpfAlreadyExists
    // TODO: shouldSignInSuccessfully
    // TODO: shouldThrowWhenUserNotFound
    // TODO: shouldThrowWhenPasswordIsWrong
    // TODO: shouldThrowWhenAccountIsDeactivated
}
