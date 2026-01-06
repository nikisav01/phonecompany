package org.phonecompany.billing.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PhoneNumberTest {

    @Test
    void shouldThrowExceptionForNullValue() {
        assertThrows(IllegalArgumentException.class, () -> new PhoneNumber(null));
    }

    @Test
    void shouldThrowExceptionForEmptyValue() {
        assertThrows(IllegalArgumentException.class, () -> new PhoneNumber(""));
    }

    @Test
    void shouldThrowExceptionForBlankValue() {
        assertThrows(IllegalArgumentException.class, () -> new PhoneNumber("   "));
    }

    @Test
    void shouldThrowExceptionForNonDigitCharacters() {
        assertThrows(IllegalArgumentException.class, () -> new PhoneNumber("420abc123"));
    }

    @Test
    void shouldAcceptValidPhoneNumber() {
        PhoneNumber phoneNumber = new PhoneNumber("420774577453");

        assertEquals("420774577453", phoneNumber.value());
    }

    @Test
    void shouldCompareEqualLengthNumbersLexicographically() {
        PhoneNumber lower = new PhoneNumber("420111111111");
        PhoneNumber higher = new PhoneNumber("420999999999");

        assertTrue(lower.compareTo(higher) < 0);
        assertTrue(higher.compareTo(lower) > 0);
        assertEquals(0, lower.compareTo(lower));
    }

    @Test
    void shouldCompareDifferentLengthNumbersByLength() {
        PhoneNumber shorter = new PhoneNumber("999");
        PhoneNumber longer = new PhoneNumber("1000");

        assertTrue(shorter.compareTo(longer) < 0);
        assertTrue(longer.compareTo(shorter) > 0);
    }

    @Test
    void shouldHandleArithmeticComparisonEdgeCase() {
        // "9" vs "10" - lexicographically "9" > "10", but arithmetically 10 > 9
        PhoneNumber nine = new PhoneNumber("9");
        PhoneNumber ten = new PhoneNumber("10");

        assertTrue(nine.compareTo(ten) < 0, "9 should be less than 10 arithmetically");
    }

    @Test
    void shouldReturnValueInToString() {
        PhoneNumber phoneNumber = new PhoneNumber("420774577453");

        assertEquals("420774577453", phoneNumber.toString());
    }
}