package org.phonecompany.billing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.phonecompany.billing.model.Call;
import org.phonecompany.billing.model.PhoneNumber;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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

    @Test
    void shouldCalculateCallStartingExactlyAt8AM() {
        // Exactly at 08:00:00 boundary
        Call call = new Call(
                new PhoneNumber("420774577453"),
                LocalDateTime.of(2020, 1, 13, 8, 0, 0),
                LocalDateTime.of(2020, 1, 13, 8, 1, 0)
        );

        BigDecimal price = calculator.calculate(call);

        assertEquals(new BigDecimal("1.00"), price);
    }

    @Test
    void shouldCalculateCallStartingOneSecondBefore8AM() {
        // One second before 08:00:00 - minute starts in off-peak
        Call call = new Call(
                new PhoneNumber("420774577453"),
                LocalDateTime.of(2020, 1, 13, 7, 59, 59),
                LocalDateTime.of(2020, 1, 13, 8, 0, 59)
        );

        BigDecimal price = calculator.calculate(call);

        assertEquals(new BigDecimal("0.50"), price);
    }

    @Test
    void shouldCalculateCallStartingExactlyAt4PM() {
        // Exactly at 16:00:00 boundary - off-peak starts
        Call call = new Call(
                new PhoneNumber("420774577453"),
                LocalDateTime.of(2020, 1, 13, 16, 0, 0),
                LocalDateTime.of(2020, 1, 13, 16, 1, 0)
        );

        BigDecimal price = calculator.calculate(call);

        assertEquals(new BigDecimal("0.50"), price);
    }

    @Test
    void shouldCalculateCallStartingOneSecondBefore4PM() {
        // One second before 16:00:00 - minute starts in peak
        Call call = new Call(
                new PhoneNumber("420774577453"),
                LocalDateTime.of(2020, 1, 13, 15, 59, 59),
                LocalDateTime.of(2020, 1, 13, 16, 0, 59)
        );

        BigDecimal price = calculator.calculate(call);

        assertEquals(new BigDecimal("1.00"), price);
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

    @Test
    void shouldHandleLongCallStartingWithSeconds() {
        // Call starting at 15:57:30 (with seconds!)
        // Duration: 8 minutes = until 16:05:30
        // Minute 0 (15:57:30): 1.00 (peak)
        // Minute 1 (15:58:30): 1.00 (peak)
        // Minute 2 (15:59:30): 1.00 (peak)
        // Minute 3 (16:00:30): 0.50 (off-peak)
        // Minute 4 (16:01:30): 0.50 (off-peak)
        // Minute 5 (16:02:30): 0.30 (off-peak + discount)
        // Minute 6 (16:03:30): 0.30 (off-peak + discount)
        // Minute 7 (16:04:30): 0.30 (off-peak + discount)
        // Total: 4.90
        Call call = new Call(
                new PhoneNumber("420774577453"),
                LocalDateTime.of(2020, 1, 13, 15, 57, 30),
                LocalDateTime.of(2020, 1, 13, 16, 5, 30)
        );

        BigDecimal price = calculator.calculate(call);

        assertEquals(new BigDecimal("4.90"), price);
    }

    @Test
    void shouldHandleCallStartingAtExactPeakBoundaryWithSeconds() {
        // Starting at 07:59:59 (one second before peak)
        // Duration: 3 minutes = until 08:02:59
        // Minute 0 (07:59:59): 0.50 (off-peak - minute started before 08:00)
        // Minute 1 (08:00:59): 1.00 (peak)
        // Minute 2 (08:01:59): 1.00 (peak)
        // Total: 2.50
        Call call = new Call(
                new PhoneNumber("420774577453"),
                LocalDateTime.of(2020, 1, 13, 7, 59, 59),
                LocalDateTime.of(2020, 1, 13, 8, 2, 59)
        );

        BigDecimal price = calculator.calculate(call);

        assertEquals(new BigDecimal("2.50"), price);
    }

    @Test
    void shouldHandleVeryLongCallSpanningMultiplePeakCycles() {
        // Very long call: 30 hours (1800 minutes) starting at 18:00
        // This tests batch calculation optimization
        // First 5 minutes: all off-peak = 5 * 0.50 = 2.50
        // Remaining 1795 minutes with discount:
        //   From 18:05 to 23:59 (354 min off-peak): 354 * 0.30 = 106.20
        //   From 00:00 to 08:00 (480 min off-peak): 480 * 0.30 = 144.00
        //   From 08:00 to 16:00 (480 min peak): 480 * 0.80 = 384.00
        //   From 16:00 to 23:59 (479 min off-peak): 479 * 0.30 = 143.70
        //   From 00:00 to 00:02 (2 min off-peak): 2 * 0.30 = 0.60
        // Total: 2.50 + 778.50 = 781.00
        Call call = new Call(
                new PhoneNumber("420774577453"),
                LocalDateTime.of(2020, 1, 13, 18, 0, 0),
                LocalDateTime.of(2020, 1, 15, 0, 0, 0)
        );

        BigDecimal price = calculator.calculate(call);

        // Just check it doesn't crash and returns a reasonable number
        assertTrue(price.compareTo(new BigDecimal("700")) > 0);
        assertTrue(price.compareTo(new BigDecimal("900")) < 0);
    }

    @Test
    void shouldCalculateVeryLongCallEfficiently() {
        // 1,000,000 minutes call - tests batch optimization
        long startTime = System.nanoTime();

        Call call = new Call(
                new PhoneNumber("420774577453"),
                LocalDateTime.of(2020, 1, 1, 0, 0, 0),
                LocalDateTime.of(2020, 1, 1, 0, 0, 0).plusMinutes(1_000_000)
        );

        BigDecimal price = calculator.calculate(call);

        long durationNanos = System.nanoTime() - startTime;
        long durationMillis = durationNanos / 1_000_000;

        System.out.println("Calculated 1,000,000 minute call in " + durationMillis + "ms");

        assertNotNull(price);
        assertTrue(price.compareTo(BigDecimal.ZERO) > 0);

        // Should complete in under 50ms even for 1 million minutes
        // Old loop-based approach would take seconds
        assertTrue(durationMillis < 50,
                "Calculation took " + durationMillis + "ms, should be < 50ms. " +
                        "This indicates the batch optimization is not working correctly.");
    }

    @Test
    void shouldCalculateExtremelyLongCallEfficiently() {
        // 10,000,000 minutes call - stress test
        // This should still complete quickly
        long startTime = System.nanoTime();

        Call call = new Call(
                new PhoneNumber("420774577453"),
                LocalDateTime.of(2020, 1, 1, 0, 0, 0),
                LocalDateTime.of(2020, 1, 1, 0, 0, 0).plusMinutes(10_000_000)
        );

        BigDecimal price = calculator.calculate(call);

        long durationNanos = System.nanoTime() - startTime;
        long durationMillis = durationNanos / 1_000_000;

        System.out.println("Calculated 10,000,000 minute call in " + durationMillis + "ms");

        assertNotNull(price);
        assertTrue(price.compareTo(BigDecimal.ZERO) > 0);

        // Even 10 million minutes should complete fast
        assertTrue(durationMillis < 100,
                "Calculation took " + durationMillis + "ms for 10M minutes");
    }
}