package org.phonecompany.billing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.phonecompany.billing.model.Call;
import org.phonecompany.billing.model.PhoneNumber;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class CallPriceCalculatorTest {

    private CallPriceCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new CallPriceCalculator();
    }

    @Test
    void shouldReturnZeroForZeroDurationCall() {
        Call call = new Call(
                new PhoneNumber("420774577453"),
                LocalDateTime.of(2020, 1, 13, 10, 0, 0),
                LocalDateTime.of(2020, 1, 13, 10, 0, 0)
        );

        BigDecimal price = calculator.calculate(call);

        assertEquals(BigDecimal.ZERO, price);
    }

    @Test
    void shouldCalculateShortCallDuringPeakHours() {
        // 3 minutes during peak time: 3 * 1.00 = 3.00
        Call call = new Call(
                new PhoneNumber("420774577453"),
                LocalDateTime.of(2020, 1, 13, 10, 0, 0),
                LocalDateTime.of(2020, 1, 13, 10, 3, 0)
        );

        BigDecimal price = calculator.calculate(call);

        assertEquals(new BigDecimal("3.00"), price);
    }

    @Test
    void shouldCalculateShortCallDuringOffPeakHours() {
        // 3 minutes during off-peak time: 3 * 0.50 = 1.50
        Call call = new Call(
                new PhoneNumber("420774577453"),
                LocalDateTime.of(2020, 1, 13, 18, 0, 0),
                LocalDateTime.of(2020, 1, 13, 18, 3, 0)
        );

        BigDecimal price = calculator.calculate(call);

        assertEquals(new BigDecimal("1.50"), price);
    }

    @Test
    void shouldCalculateExactly5MinutesCallInPeakHours() {
        // Exactly 5 minutes, no discount applied
        Call call = new Call(
                new PhoneNumber("420774577453"),
                LocalDateTime.of(2020, 1, 13, 10, 0, 0),
                LocalDateTime.of(2020, 1, 13, 10, 5, 0)
        );

        BigDecimal price = calculator.calculate(call);

        assertEquals(new BigDecimal("5.00"), price);
    }

    @Test
    void shouldApplyDiscountForLongCallInPeakHours() {
        // 7 minutes during peak time:
        // Minutes 0-4: 5 * 1.00 = 5.00
        // Minute 5: 1.00 - 0.20 = 0.80
        // Minute 6: 1.00 - 0.20 = 0.80
        // Total: 6.60
        Call call = new Call(
                new PhoneNumber("420774577453"),
                LocalDateTime.of(2020, 1, 13, 10, 0, 0),
                LocalDateTime.of(2020, 1, 13, 10, 7, 0)
        );

        BigDecimal price = calculator.calculate(call);

        assertEquals(new BigDecimal("6.60"), price);
    }

    @Test
    void shouldApplyDiscountForLongCallInOffPeakHours() {
        // 7 minutes during off-peak time:
        // Minutes 0-4: 5 * 0.50 = 2.50
        // Minute 5: 0.50 - 0.20 = 0.30
        // Minute 6: 0.50 - 0.20 = 0.30
        // Total: 3.10
        Call call = new Call(
                new PhoneNumber("420774577453"),
                LocalDateTime.of(2020, 1, 13, 18, 0, 0),
                LocalDateTime.of(2020, 1, 13, 18, 7, 0)
        );

        BigDecimal price = calculator.calculate(call);

        assertEquals(new BigDecimal("3.10"), price);
    }

    @Test
    void shouldHandleVeryLongCall() {
        // 20 minutes during peak time:
        // Minutes 0-4: 5 * 1.00 = 5.00
        // Minutes 5-19: 15 * (1.00 - 0.20) = 15 * 0.80 = 12.00
        // Total: 17.00
        Call call = new Call(
                new PhoneNumber("420774577453"),
                LocalDateTime.of(2020, 1, 13, 10, 0, 0),
                LocalDateTime.of(2020, 1, 13, 10, 20, 0)
        );

        BigDecimal price = calculator.calculate(call);

        assertEquals(new BigDecimal("17.00"), price);
    }

    @Test
    void shouldHandleCallSpanningPeakAndOffPeakHours() {
        // Call from 15:58 to 16:02 (4 minutes)
        // Minute 0 (15:58): 1.00 (peak)
        // Minute 1 (15:59): 1.00 (peak)
        // Minute 2 (16:00): 0.50 (off-peak)
        // Minute 3 (16:01): 0.50 (off-peak)
        // Total: 3.00
        Call call = new Call(
                new PhoneNumber("420774577453"),
                LocalDateTime.of(2020, 1, 13, 15, 58, 0),
                LocalDateTime.of(2020, 1, 13, 16, 2, 0)
        );

        BigDecimal price = calculator.calculate(call);

        assertEquals(new BigDecimal("3.00"), price);
    }

    @Test
    void shouldHandleLongCallSpanningPeakAndOffPeakHours() {
        // Call from 15:57 to 16:05 (8 minutes)
        // Minute 0 (15:57): 1.00 (peak)
        // Minute 1 (15:58): 1.00 (peak)
        // Minute 2 (15:59): 1.00 (peak)
        // Minute 3 (16:00): 0.50 (off-peak)
        // Minute 4 (16:01): 0.50 (off-peak)
        // Minute 5 (16:02): 0.50 - 0.20 = 0.30 (off-peak + discount)
        // Minute 6 (16:03): 0.50 - 0.20 = 0.30 (off-peak + discount)
        // Minute 7 (16:04): 0.50 - 0.20 = 0.30 (off-peak + discount)
        // Total: 4.90
        Call call = new Call(
                new PhoneNumber("420774577453"),
                LocalDateTime.of(2020, 1, 13, 15, 57, 0),
                LocalDateTime.of(2020, 1, 13, 16, 5, 0)
        );

        BigDecimal price = calculator.calculate(call);

        assertEquals(new BigDecimal("4.90"), price);
    }

    @Test
    void shouldHandleCallStartingBeforePeakHours() {
        // Call from 07:58 to 08:02 (4 minutes)
        // Minute 0 (07:58): 0.50 (off-peak)
        // Minute 1 (07:59): 0.50 (off-peak)
        // Minute 2 (08:00): 1.00 (peak)
        // Minute 3 (08:01): 1.00 (peak)
        // Total: 3.00
        Call call = new Call(
                new PhoneNumber("420774577453"),
                LocalDateTime.of(2020, 1, 13, 7, 58, 0),
                LocalDateTime.of(2020, 1, 13, 8, 2, 0)
        );

        BigDecimal price = calculator.calculate(call);

        assertEquals(new BigDecimal("3.00"), price);
    }

    @Test
    void shouldRoundUpPartialMinutes() {
        // 1 second should count as 1 minute
        Call call = new Call(
                new PhoneNumber("420774577453"),
                LocalDateTime.of(2020, 1, 13, 10, 0, 0),
                LocalDateTime.of(2020, 1, 13, 10, 0, 1)
        );

        BigDecimal price = calculator.calculate(call);

        assertEquals(new BigDecimal("1.00"), price);
    }

    @Test
    void shouldRoundUp59Seconds() {
        // 59 seconds = 1 minute
        Call call = new Call(
                new PhoneNumber("420774577453"),
                LocalDateTime.of(2020, 1, 13, 10, 0, 0),
                LocalDateTime.of(2020, 1, 13, 10, 0, 59)
        );

        BigDecimal price = calculator.calculate(call);

        assertEquals(new BigDecimal("1.00"), price);
    }

    @Test
    void shouldRoundUp61SecondsTo2Minutes() {
        // 61 seconds = 2 minutes
        Call call = new Call(
                new PhoneNumber("420774577453"),
                LocalDateTime.of(2020, 1, 13, 10, 0, 0),
                LocalDateTime.of(2020, 1, 13, 10, 1, 1)
        );

        BigDecimal price = calculator.calculate(call);

        assertEquals(new BigDecimal("2.00"), price);
    }

    @Test
    void shouldHandleExampleFromTask1() {
        // Example 1: 13-01-2020 18:10:15 to 18:12:57
        // Duration: 2:42 = 3 minutes (rounded up)
        // 18:10, 18:11, 18:12 - off-peak: 3 * 0.50 = 1.50
        Call call = new Call(
                new PhoneNumber("420774577453"),
                LocalDateTime.of(2020, 1, 13, 18, 10, 15),
                LocalDateTime.of(2020, 1, 13, 18, 12, 57)
        );

        BigDecimal price = calculator.calculate(call);

        assertEquals(new BigDecimal("1.50"), price);
    }

    @Test
    void shouldHandleExampleFromTask2() {
        // Example 2: 18-01-2020 08:59:20 to 09:10:00
        // Duration: 10:40 = 11 minutes
        // Minutes 0-4 (08:59-09:03): 5 * 1.00 = 5.00 (peak)
        // Minutes 5-10 (09:04-09:09): 6 * (1.00 - 0.20) = 6 * 0.80 = 4.80 (peak + discount)
        // Total: 9.80
        Call call = new Call(
                new PhoneNumber("420776562353"),
                LocalDateTime.of(2020, 1, 18, 8, 59, 20),
                LocalDateTime.of(2020, 1, 18, 9, 10, 0)
        );

        BigDecimal price = calculator.calculate(call);

        assertEquals(new BigDecimal("9.80"), price);
    }

    @ParameterizedTest
    @MethodSource("providePeakHourBoundaries")
    void shouldCorrectlyHandlePeakHourBoundaries(LocalDateTime start, LocalDateTime end, String expectedPrice) {
        Call call = new Call(new PhoneNumber("420774577453"), start, end);

        BigDecimal price = calculator.calculate(call);

        assertEquals(new BigDecimal(expectedPrice), price);
    }

    private static Stream<Arguments> providePeakHourBoundaries() {
        return Stream.of(
                // Exactly at 08:00:00 boundary
                Arguments.of(
                        LocalDateTime.of(2020, 1, 13, 8, 0, 0),
                        LocalDateTime.of(2020, 1, 13, 8, 1, 0),
                        "1.00"
                ),
                // One second before 08:00:00
                Arguments.of(
                        LocalDateTime.of(2020, 1, 13, 7, 59, 59),
                        LocalDateTime.of(2020, 1, 13, 8, 0, 59),
                        "0.50"
                ),
                // Exactly at 16:00:00 boundary
                Arguments.of(
                        LocalDateTime.of(2020, 1, 13, 16, 0, 0),
                        LocalDateTime.of(2020, 1, 13, 16, 1, 0),
                        "0.50"
                ),
                // One second before 16:00:00
                Arguments.of(
                        LocalDateTime.of(2020, 1, 13, 15, 59, 59),
                        LocalDateTime.of(2020, 1, 13, 16, 0, 59),
                        "1.00"
                )
        );
    }

    @Test
    void shouldHandleMidnightCall() {
        // Call spanning midnight
        Call call = new Call(
                new PhoneNumber("420774577453"),
                LocalDateTime.of(2020, 1, 13, 23, 58, 0),
                LocalDateTime.of(2020, 1, 14, 0, 2, 0)
        );

        BigDecimal price = calculator.calculate(call);

        // 4 minutes off-peak: 4 * 0.50 = 2.00
        assertEquals(new BigDecimal("2.00"), price);
    }

    @Test
    void shouldHandleCallSpanningMultipleDays() {
        // Long call spanning a day
        Call call = new Call(
                new PhoneNumber("420774577453"),
                LocalDateTime.of(2020, 1, 13, 23, 0, 0),
                LocalDateTime.of(2020, 1, 14, 1, 0, 0)
        );

        BigDecimal price = calculator.calculate(call);

        // 120 minutes:
        // First 5 minutes (23:00-23:04): 5 * 0.50 = 2.50
        // Remaining 115 minutes: 115 * (0.50 - 0.20) = 115 * 0.30 = 34.50
        // Total: 37.00
        assertEquals(new BigDecimal("37.00"), price);
    }
}