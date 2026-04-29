package com.sysmap.hubapi.controller;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(AuthController.class)
class AuthControllerTest {
    // TODO: shouldReturn201WhenRegisterIsSuccessful
    // TODO: shouldReturn400WhenRegisterFieldsAreMissing
    // TODO: shouldReturn409WhenEmailOrCpfAlreadyExists
    // TODO: shouldReturn200WithTokenOnSignIn
    // TODO: shouldReturn401WhenPasswordIsWrong
    // TODO: shouldReturn403WhenAccountIsDeactivated
    // TODO: shouldReturn404WhenUserNotFound
}
