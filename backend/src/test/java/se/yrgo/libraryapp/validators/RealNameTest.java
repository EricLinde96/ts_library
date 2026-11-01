package se.yrgo.libraryapp.validators;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class RealNameTest {

    @ParameterizedTest
    @ValueSource(strings = {"badword", "yuckie", "damn", "turds", "cat", "clown", "ghosts", "devil"})
    void invalidWhenNameIsBadWord(String name) {
        assertThat(RealName.validate(name)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"b4dw0rd", "yuckI3", "d4mn", "7urd5", "cl0wn", "6h0575", "d3vil", "C4T", "ClOwN"})
    void invalidWhenBadWordIsDisguisedWithLeetOrUpperCase(String name) {
        assertThat(RealName.validate(name)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"cat Linde", "Eric cat", "Eric cat Linde", "Eric  cat"})
    void invalidWhenBadWordIsAnywhereInName(String name) {
        assertThat(RealName.validate(name)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"Anna", "Eric Linde", "Åsa", "Catherine", "Damnation Smith", ""})
    void validForOrdinaryNames(String name) {
        assertThat(RealName.validate(name)).isTrue();
    }
}