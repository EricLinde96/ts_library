package se.yrgo.libraryapp.validators;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

public class UsernameTest {
    @ParameterizedTest
    @ValueSource(strings = {"bosse", "user1", "eric.linde", "bark_not", "pelle-plupp", "x@y.se"})
    void validUsernames(String username) {
        assertThat(Username.validate(username)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"name with spacing", "bosse!", "äric.linde", "bo", "*.,olof"})
    void invalidUsernames(String username) {
        assertThat(Username.validate(username)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"eric", "e.l_"})
    void usernameWithFourCharactersIsValid(String username) {
        assertThat(Username.validate(username)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"eri", "e.l"})
    void usernameWithThreeCharactersIsInvalid(String username) {
        assertThat(Username.validate(username)).isFalse();
    }

    @ParameterizedTest
    @NullAndEmptySource
    void nullAndEmptyUsernameIsInvalid(String username) {
        assertThat(Username.validate(username)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"    ", "      "})
    void usernameWithOnlySpacesIsInvalid(String username) {
        assertThat(Username.validate(username)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"@._-", "----", "...."})
    void usernameWithOnlySpecialCharactersIsValid(String username) {
        assertThat(Username.validate(username)).isTrue();
    }
}