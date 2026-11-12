package se.yrgo.libraryapp.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.pac4j.core.exception.CredentialsException;

import io.jooby.Context;
import io.jooby.Cookie;
import io.jooby.StatusCode;
import se.yrgo.libraryapp.dao.RoleDao;
import se.yrgo.libraryapp.dao.SessionDao;
import se.yrgo.libraryapp.entities.Role;
import se.yrgo.libraryapp.entities.UserId;
import se.yrgo.libraryapp.entities.forms.LoginData;
import se.yrgo.libraryapp.services.UserService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
public class LoginControllerTest {
    @Mock
    private UserService userService;

    @Mock
    private RoleDao roleDao;

    @Mock
    private SessionDao sessionDao;

    @Mock
    private Context context;

    private LoginController controller;

    @BeforeEach
    void createController() {
        controller = new LoginController(userService, roleDao, sessionDao);
    }

    // login

    @Test
    void successfulLoginReturnsRolesAndSetsSessionCookie() {
        final UserId userId = UserId.of(1);
        final UUID sessionId = UUID.randomUUID();

        when(userService.validate("test", "password")).thenReturn(Optional.of(userId));
        when(sessionDao.create(userId)).thenReturn(sessionId);
        when(roleDao.get(userId)).thenReturn(List.of(Role.USER));

        List<Role> roles = controller.login(context, null, new LoginData("test", "password"));

        assertThat(roles).containsExactly(Role.USER);

        ArgumentCaptor<Cookie> cookie = ArgumentCaptor.forClass(Cookie.class);
        verify(context).setResponseCookie(cookie.capture());
        assertThat(cookie.getValue().getName()).isEqualTo("session");
        assertThat(cookie.getValue().getValue()).isEqualTo(sessionId.toString());
        assertThat(cookie.getValue().isHttpOnly()).isTrue();
    }

    @Test
    void failedLoginGivesUnauthorizedAndNoSession() {
        when(userService.validate("test", "wrong")).thenReturn(Optional.empty());

        List<Role> roles = controller.login(context, null, new LoginData("test", "wrong"));

        assertThat(roles).isEmpty();
        verify(context).setResponseCode(StatusCode.UNAUTHORIZED);
        verify(sessionDao, never()).create(any());
    }

    @Test
    void alreadyLoggedInDoesNotCheckPasswordAgain() {
        final UUID existingSession = UUID.randomUUID();

        when(sessionDao.validate(existingSession)).thenReturn(UserId.of(1));

        List<Role> roles = controller.login(context, existingSession.toString(),
                new LoginData("test", "password"));

        assertThat(roles).isEmpty();
        verify(userService, never()).validate(any(), any());
    }

    //isLoggedIn

    @Test
    void validSessionReturnsRoles() {
        final UserId userId = UserId.of(1);
        final UUID session = UUID.randomUUID();

        when(sessionDao.validate(session)).thenReturn(userId);
        when(roleDao.get(userId)).thenReturn(List.of(Role.ADMIN));

        assertThat(controller.isLoggedIn(session.toString())).containsExactly(Role.ADMIN);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "not-a-uuid"})
    void missingOrMalformedCookieReturnsEmpty(String cookie) {
        assertThat(controller.isLoggedIn(cookie)).isEmpty();
    }

    @Test
    void unknownSessionReturnsEmpty() {
        final UUID unknown = UUID.randomUUID();

        when(sessionDao.validate(unknown)).thenThrow(new CredentialsException("no such session"));

        assertThat(controller.isLoggedIn(unknown.toString())).isEmpty();
    }
}