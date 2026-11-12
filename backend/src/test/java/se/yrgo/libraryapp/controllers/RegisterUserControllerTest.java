package se.yrgo.libraryapp.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import se.yrgo.libraryapp.entities.forms.RegisterUserData;
import se.yrgo.libraryapp.services.UserService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
public class RegisterUserControllerTest {
    @Mock
    private UserService userService;

    private RegisterUserController controller;

    @BeforeEach
    void createController() {
        controller = new RegisterUserController(userService);
    }

    @Test
    void validDataIsRegisteredThroughService() {
        when(userService.register("testuser", "Eric Linde", "secret")).thenReturn(true);

        boolean result = controller.register(
                new RegisterUserData("testuser", "Eric Linde", "secret"));

        assertThat(result).isTrue();
    }

    @Test
    void invalidUsernameIsRejectedWithoutCallingService() {
        boolean result = controller.register(
                new RegisterUserData("ab", "Eric Linde", "secret"));

        assertThat(result).isFalse();
        verify(userService, never()).register(any(), any(), any());
    }

    @Test
    void nameAvailabilityComesFromService() {
        when(userService.isNameAvailable("free")).thenReturn(true);

        assertThat(controller.isNameAvailable("free")).isTrue();
    }
}
