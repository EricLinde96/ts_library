package se.yrgo.libraryapp.validators;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class UtilsTest {
    @ParameterizedTest
    @CsvSource({
            "abc, abc",
            "ABC, abc",
            "HeJ, hej"
    })
    void onlyLettersKeepsLettersAsLowercase(String input, String expected) {
        assertThat(Utils.onlyLettersAndWhitespace(input)).isEqualTo(expected);
    }
    @ParameterizedTest
    @CsvSource({
            "a1b2c3, abc",
            "hej!?, hej",
            "'hej då', 'hej då'",
            "'hej 123', 'hej '"
    })
    void onlyLettersRemovesNonLettersButKeepsWhitespace(String input, String expected) {
        assertThat(Utils.onlyLettersAndWhitespace(input)).isEqualTo(expected);
    }
    @ParameterizedTest
    @ValueSource(strings = {"", "123", "!?#", "@._-"})
    void onlyLettersReturnsEmptyWhenNoLetters(String input) {
        assertThat(Utils.onlyLettersAndWhitespace(input)).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({"ÅÄÖ, åäö", "Smörgåsbord, smörgåsbord"})
    void handlesSwedishLetters(String input, String expected) {
        assertThat(Utils.onlyLettersAndWhitespace(input)).isEqualTo(expected);
    }
    @ParameterizedTest
    @CsvSource({
            "4, a",
            "3, e",
            "1, l",
            "5, s",
            "7, t",
            "0, o",
            "8, b",
            "6, g"
    })
    void convertsEachLeetCharacter(String input, String expected) {
        assertThat(Utils.cleanAndUnLeet(input)).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
            "h4ll0, hallo",
            "1337, leet",
            "H3J, hej",
            "'b4d w0rd5', 'bad words'",
            "'t3st 123', 'test le'",   // 2 saknar mappning och tas bort
            "h3j!?#, hej"
    })
    void unLeetsAndRemovesNonLettersButKeepsWhitespace(String input, String expected) {
        assertThat(Utils.cleanAndUnLeet(input)).isEqualTo(expected);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "2", "!?#", "@._-"})
    void returnsEmptyWhenNothingLeftAfterCleaning(String input) {
        assertThat(Utils.cleanAndUnLeet(input)).isEmpty();
    }

    @Test
    void keepsWhitespaceOnly() {
        assertThat(Utils.cleanAndUnLeet(" \t")).isEqualTo(" \t");
    }


}