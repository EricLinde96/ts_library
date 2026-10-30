package se.yrgo.libraryapp.validators;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class UsernameTest {
    @Test
    void correctUsername() {
        assertThat(Username.validate("bosse")).isTrue();
    }

    @Test
    void incorrectUsername() {
        assertThat(Username.validate("name with space")).isFalse();
    }
}