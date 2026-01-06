package org.phonecompany.billing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class TelephoneBillCalculatorImplTest {

    private TelephoneBillCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new TelephoneBillCalculatorImpl();
    }

    @Test
    void shouldReturnZeroForEmptyLog() {
        BigDecimal result = calculator.calculate("");

        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    void shouldReturnZeroForNullLog() {
        BigDecimal result = calculator.calculate(null);

        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    void shouldCalculateSingleCall() {
        // 3 minutes during peak time: 3 * 1.00 = 3.00
        // But it's the most called number, so it's free
        String log = "420774577453,13-01-2020 10:00:00,13-01-2020 10:03:00";

        BigDecimal result = calculator.calculate(log);

        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    void shouldCalculateMultipleCallsWithPromotion() {
        String log = """
            420774577453,13-01-2020 10:00:00,13-01-2020 10:03:00
            420774577453,13-01-2020 11:00:00,13-01-2020 11:02:00
            420776562353,13-01-2020 12:00:00,13-01-2020 12:05:00
            """;

        // 420774577453: 2 calls (most called) - FREE
        // 420776562353: 1 call, 5 minutes peak time: 5 * 1.00 = 5.00

        BigDecimal result = calculator.calculate(log);

        assertEquals(new BigDecimal("5.00"), result);
    }

    @Test
    void shouldApplyPromotionToArithmeticallyHigherNumberWhenTied() {
        String log = """
            420111111111,13-01-2020 10:00:00,13-01-2020 10:03:00
            420999999999,13-01-2020 10:00:00,13-01-2020 10:03:00
            """;

        // Both numbers have 1 call - 420999999999 is arithmetically higher, so it's free
        // 420111111111: 3 minutes peak time: 3 * 1.00 = 3.00

        BigDecimal result = calculator.calculate(log);

        assertEquals(new BigDecimal("3.00"), result);
    }

    @Test
    void shouldHandleExampleFromTask() {
        String log = """
            420774577453,13-01-2020 18:10:15,13-01-2020 18:12:57
            420776562353,18-01-2020 08:59:20,18-01-2020 09:10:00
            """;

        // Both numbers have 1 call - 420776562353 is arithmetically higher, so it's free
        // 420774577453: 3 minutes off-peak: 3 * 0.50 = 1.50

        BigDecimal result = calculator.calculate(log);

        assertEquals(new BigDecimal("1.50"), result);
    }

    @Test
    void shouldCalculateComplexScenarioWithMultipleCalls() {
        String log = """
            420774577453,13-01-2020 10:00:00,13-01-2020 10:03:00
            420774577453,13-01-2020 11:00:00,13-01-2020 11:02:00
            420774577453,13-01-2020 12:00:00,13-01-2020 12:04:00
            420776562353,13-01-2020 13:00:00,13-01-2020 13:05:00
            420776562353,13-01-2020 14:00:00,13-01-2020 14:03:00
            420111222333,13-01-2020 15:00:00,13-01-2020 15:10:00
            """;

        // 420774577453: 3 calls (most called) - FREE
        // 420776562353: 2 calls
        //   - Call 1: 5 minutes peak: 5 * 1.00 = 5.00
        //   - Call 2: 3 minutes peak: 3 * 1.00 = 3.00
        //   Total: 8.00
        // 420111222333: 1 call, 10 minutes peak
        //   - First 5 minutes: 5 * 1.00 = 5.00
        //   - Next 5 minutes: 5 * (1.00 - 0.20) = 4.00
        //   Total: 9.00
        // Grand total: 8.00 + 9.00 = 17.00

        BigDecimal result = calculator.calculate(log);

        assertEquals(new BigDecimal("17.00"), result);
    }

    @Test
    void shouldHandleLongCallsWithDiscount() {
        String log = """
            420774577453,13-01-2020 10:00:00,13-01-2020 10:10:00
            420776562353,13-01-2020 18:00:00,13-01-2020 18:08:00
            """;

        // Both have 1 call - 420776562353 is higher, so it's free
        // 420774577453: 10 minutes peak
        //   - First 5 minutes: 5 * 1.00 = 5.00
        //   - Next 5 minutes: 5 * (1.00 - 0.20) = 4.00
        //   Total: 9.00

        BigDecimal result = calculator.calculate(log);

        assertEquals(new BigDecimal("9.00"), result);
    }

    @Test
    void shouldHandleCallsSpanningPeakAndOffPeakHours() {
        String log = """
            420774577453,13-01-2020 15:58:00,13-01-2020 16:02:00
            420776562353,13-01-2020 10:00:00,13-01-2020 10:02:00
            """;

        // Both have 1 call - 420776562353 is higher, so it's free
        // 420774577453: 4 minutes spanning peak/off-peak
        //   - Minute 0 (15:58): 1.00 (peak)
        //   - Minute 1 (15:59): 1.00 (peak)
        //   - Minute 2 (16:00): 0.50 (off-peak)
        //   - Minute 3 (16:01): 0.50 (off-peak)
        //   Total: 3.00

        BigDecimal result = calculator.calculate(log);

        assertEquals(new BigDecimal("3.00"), result);
    }

    @Test
    void shouldHandleMixedPeakAndOffPeakCalls() {
        String log = """
            420774577453,13-01-2020 10:00:00,13-01-2020 10:05:00
            420774577453,13-01-2020 18:00:00,13-01-2020 18:05:00
            420776562353,13-01-2020 14:00:00,13-01-2020 14:03:00
            """;

        // 420774577453: 2 calls (most called) - FREE
        // 420776562353: 1 call, 3 minutes peak: 3 * 1.00 = 3.00

        BigDecimal result = calculator.calculate(log);

        assertEquals(new BigDecimal("3.00"), result);
    }

    @Test
    void shouldHandleAllCallsToSameNumber() {
        String log = """
            420774577453,13-01-2020 10:00:00,13-01-2020 10:03:00
            420774577453,13-01-2020 11:00:00,13-01-2020 11:02:00
            420774577453,13-01-2020 12:00:00,13-01-2020 12:04:00
            """;

        // All calls to the same number - it's the most called, so all are free

        BigDecimal result = calculator.calculate(log);

        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    void shouldHandleVeryShortCalls() {
        String log = """
            420774577453,13-01-2020 10:00:00,13-01-2020 10:00:01
            420776562353,13-01-2020 10:00:00,13-01-2020 10:00:01
            """;

        // Both have 1 call of 1 second (1 minute) - 420776562353 is higher, so it's free
        // 420774577453: 1 minute peak: 1 * 1.00 = 1.00

        BigDecimal result = calculator.calculate(log);

        assertEquals(new BigDecimal("1.00"), result);
    }

    @Test
    void shouldHandleRoundingUpMinutes() {
        String log = """
            420774577453,13-01-2020 10:00:00,13-01-2020 10:02:30
            420776562353,13-01-2020 10:00:00,13-01-2020 10:01:00
            """;

        // Both have 1 call - 420776562353 is higher, so it's free
        // 420774577453: 2:30 = 3 minutes peak: 3 * 1.00 = 3.00

        BigDecimal result = calculator.calculate(log);

        assertEquals(new BigDecimal("3.00"), result);
    }

    @Test
    void shouldHandleRealWorldScenario() {
        String log = """
            420774577453,13-01-2020 08:00:00,13-01-2020 08:05:30
            420774577453,13-01-2020 12:00:00,13-01-2020 12:03:00
            420776562353,13-01-2020 09:00:00,13-01-2020 09:12:00
            420776562353,13-01-2020 16:00:00,13-01-2020 16:04:00
            420111222333,13-01-2020 10:00:00,13-01-2020 10:02:00
            420111222333,13-01-2020 14:00:00,13-01-2020 14:08:00
            420111222333,13-01-2020 18:00:00,13-01-2020 18:03:00
            """;

        // 420111222333: 3 calls (most called) - FREE
        // 420774577453: 2 calls
        //   - Call 1: 6 minutes peak: 5*1.00 + 1*0.80 = 5.80
        //   - Call 2: 3 minutes peak: 3*1.00 = 3.00
        //   Total: 8.80
        // 420776562353: 2 calls
        //   - Call 1: 12 minutes peak: 5*1.00 + 7*0.80 = 10.60
        //   - Call 2: 4 minutes off-peak: 4*0.50 = 2.00
        //   Total: 12.60
        // Grand total: 8.80 + 12.60 = 21.40

        BigDecimal result = calculator.calculate(log);

        assertEquals(new BigDecimal("21.40"), result);
    }

    @Test
    void shouldHandleWhitespaceInLog() {
        String log = """
            
            420774577453,13-01-2020 10:00:00,13-01-2020 10:03:00
            
            420776562353,13-01-2020 10:00:00,13-01-2020 10:02:00
            
            """;

        // Should handle empty lines gracefully
        // Both have 1 call - 420776562353 is higher, so it's free
        // 420774577453: 3 minutes peak: 3 * 1.00 = 3.00

        BigDecimal result = calculator.calculate(log);

        assertEquals(new BigDecimal("3.00"), result);
    }
}