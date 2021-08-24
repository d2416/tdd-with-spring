package com.amigoscode.testing.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

public class PhoneNumberValidatorTest {

    private PhoneNumberValidator underTest;

    @BeforeEach
    public void setUp() {
        underTest = new PhoneNumberValidator();
    }

    @ParameterizedTest
    @CsvSource({
            "+33600000000, true, Correct phone number",
            "+33612345678911, false, Length is bigger than 13",
            "33612345678, false, Phone does not start with +"
    })
    void itShouldValidatePhoneNumber(String phoneNumber, boolean expected, String description) {
        // When
        boolean isValid = underTest.test(phoneNumber);

        // Then
        assertThat(isValid).describedAs(description).isEqualTo(expected);
    }

    @Test
    @DisplayName("Should fail when phone does not start with +")
    void itShouldNotValidatePhoneNumberWhenStartsOtherThanPlus() {
        // Given
        String phoneNumber = "33612345678";

        // When
        boolean isValid = underTest.test(phoneNumber);

        // Then
        assertThat(isValid).isFalse();
    }
}
