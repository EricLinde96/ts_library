package se.yrgo.libraryapp.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

import se.yrgo.libraryapp.dao.UserDao;
import se.yrgo.libraryapp.entities.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
public class UserServiceTest {
    @Mock
    private UserDao userDao;

    @Mock
    private PasswordEncoder mockEncoder;

    @Test
    @SuppressWarnings("deprecation")
    void correctLogin() {
        final String userId = "1";
        final UserId id = UserId.of(userId);
        final String username = "testuser";
        final String password = "password";
        final String passwordHash = "password";
        final LoginInfo info = new LoginInfo(id, passwordHash);
        final PasswordEncoder encoder =
                org.springframework.security.crypto.password.NoOpPasswordEncoder.getInstance();

        when(userDao.getLoginInfo(username)).thenReturn(Optional.of(info));

        UserService userService = new UserService(userDao, encoder);

        assertThat(userService.validate(username, password)).isEqualTo(Optional.of(id));
    }

    @Test
    void wrongPasswordGivesEmpty() {
        final LoginInfo info = new LoginInfo(UserId.of("1"), "hash");

        when(userDao.getLoginInfo("testuser")).thenReturn(Optional.of(info));
        when(mockEncoder.matches("wrong", "hash")).thenReturn(false);

        UserService userService = new UserService(userDao, mockEncoder);

        assertThat(userService.validate("testuser", "wrong")).isEmpty();
    }

    @Test
    void registerSendsHashedPasswordToDao() {
        when(mockEncoder.encode("secret")).thenReturn("hashed");
        when(userDao.register("testuser", "Test User", "hashed")).thenReturn(true);

        UserService userService = new UserService(userDao, mockEncoder);

        assertThat(userService.register("testuser", "Test User", "secret")).isTrue();
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "ab"})
    void tooShortNameIsNotAvailableAndDaoIsNotAsked(String name) {
        UserService userService = new UserService(userDao, mockEncoder);

        assertThat(userService.isNameAvailable(name)).isFalse();
        verify(userDao, never()).isNameAvailable(any());
    }

    @Test
    void validNameIsAvailableWhenDaoSaysSo() {
        when(userDao.isNameAvailable("abc")).thenReturn(true);

        UserService userService = new UserService(userDao, mockEncoder);

        assertThat(userService.isNameAvailable("abc")).isTrue();
    }
}