package org.phonecompany.billing.promotion;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.phonecompany.billing.model.Call;
import org.phonecompany.billing.model.PhoneNumber;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class MostCalledNumberPromotionTest {

    private MostCalledNumberPromotion promotion;
    private LocalDateTime start;
    private LocalDateTime end;

    @BeforeEach
    void setUp() {
        promotion = new MostCalledNumberPromotion();
        start = LocalDateTime.of(2020, 1, 13, 10, 0, 0);
        end = LocalDateTime.of(2020, 1, 13, 10, 5, 0);
    }

    @Test
    void shouldReturnEmptyForNullCalls() {
        Optional<PhoneNumber> result = promotion.getFreePhoneNumber(null);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnEmptyForEmptyCalls() {
        Optional<PhoneNumber> result = promotion.getFreePhoneNumber(List.of());

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnSingleNumberWhenOnlyOneNumberCalled() {
        List<Call> calls = List.of(
                new Call(new PhoneNumber("420774577453"), start, end)
        );

        Optional<PhoneNumber> result = promotion.getFreePhoneNumber(calls);

        assertTrue(result.isPresent());
        assertEquals("420774577453", result.get().value());
    }

    @Test
    void shouldReturnMostFrequentlyCalledNumber() {
        List<Call> calls = List.of(
                new Call(new PhoneNumber("420774577453"), start, end),
                new Call(new PhoneNumber("420774577453"), start, end),
                new Call(new PhoneNumber("420774577453"), start, end),
                new Call(new PhoneNumber("420776562353"), start, end),
                new Call(new PhoneNumber("420776562353"), start, end)
        );

        Optional<PhoneNumber> result = promotion.getFreePhoneNumber(calls);

        assertTrue(result.isPresent());
        assertEquals("420774577453", result.get().value());
    }

    @Test
    void shouldReturnArithmeticallyHighestNumberWhenTied() {
        List<Call> calls = List.of(
                new Call(new PhoneNumber("420111111111"), start, end),
                new Call(new PhoneNumber("420111111111"), start, end),
                new Call(new PhoneNumber("420999999999"), start, end),
                new Call(new PhoneNumber("420999999999"), start, end)
        );

        Optional<PhoneNumber> result = promotion.getFreePhoneNumber(calls);

        assertTrue(result.isPresent());
        assertEquals("420999999999", result.get().value());
    }

    @Test
    void shouldCompareNumbersArithmeticallyNotLexicographically() {
        // Lexicographically "9" > "10", but arithmetically 10 > 9
        List<Call> calls = List.of(
                new Call(new PhoneNumber("9"), start, end),
                new Call(new PhoneNumber("10"), start, end)
        );

        Optional<PhoneNumber> result = promotion.getFreePhoneNumber(calls);

        assertTrue(result.isPresent());
        assertEquals("10", result.get().value());
    }

    @Test
    void shouldHandleMultipleNumbersWithSameHighestCount() {
        List<Call> calls = List.of(
                new Call(new PhoneNumber("420111111111"), start, end),
                new Call(new PhoneNumber("420111111111"), start, end),
                new Call(new PhoneNumber("420111111111"), start, end),
                new Call(new PhoneNumber("420999999999"), start, end),
                new Call(new PhoneNumber("420999999999"), start, end),
                new Call(new PhoneNumber("420999999999"), start, end),
                new Call(new PhoneNumber("420555555555"), start, end)
        );

        Optional<PhoneNumber> result = promotion.getFreePhoneNumber(calls);

        assertTrue(result.isPresent());
        assertEquals("420999999999", result.get().value());
    }

    @Test
    void shouldHandleDifferentLengthNumbers() {
        // Longer number is arithmetically higher
        List<Call> calls = List.of(
                new Call(new PhoneNumber("999"), start, end),
                new Call(new PhoneNumber("1000"), start, end)
        );

        Optional<PhoneNumber> result = promotion.getFreePhoneNumber(calls);

        assertTrue(result.isPresent());
        assertEquals("1000", result.get().value());
    }
}