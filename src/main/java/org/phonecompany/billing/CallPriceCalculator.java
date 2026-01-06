package org.phonecompany.billing;

import org.phonecompany.billing.model.Call;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Calculates the price for a single call based on time and duration.
 * Rules:
 * - Peak hours [08:00:00, 16:00:00): 1.00 Kč per minute
 * - Off-peak hours: 0.50 Kč per minute
 * - For calls longer than 5 minutes: minutes 6+ cost 0.20 Kč regardless of time
 */
public class CallPriceCalculator {

    private static final LocalTime PEAK_START = LocalTime.of(8, 0, 0);
    private static final LocalTime PEAK_END = LocalTime.of(16, 0, 0);

    private static final BigDecimal PEAK_RATE = new BigDecimal("1.00");
    private static final BigDecimal OFF_PEAK_RATE = new BigDecimal("0.50");
    private static final BigDecimal LONG_CALL_DISCOUNT = new BigDecimal("0.20");

    private static final int STANDARD_MINUTES_THRESHOLD = 5;

    /**
     * Calculates the total price for a call.
     */
    public BigDecimal calculate(Call call) {
        long totalMinutes = call.getDurationInMinutes();
        BigDecimal total = BigDecimal.ZERO;

        if (totalMinutes == 0) {
            return total;
        }

        for (int i = 0; i < totalMinutes; i++) {
            LocalDateTime minuteStart = call.getMinuteStartTime(i);
            total = total.add(getStandardRate(minuteStart));

            if (i >= STANDARD_MINUTES_THRESHOLD) {
                total = total.subtract(LONG_CALL_DISCOUNT);
            }
        }

        return total;
    }

    /**
     * Returns the standard rate (peak or off-peak) for a given time.
     */
    private BigDecimal getStandardRate(LocalDateTime dateTime) {
        return isPeakHour(dateTime) ? PEAK_RATE : OFF_PEAK_RATE;
    }

    /**
     * Checks if the given time is within peak hours [08:00:00, 16:00:00).
     */
    private boolean isPeakHour(LocalDateTime dateTime) {
        LocalTime time = dateTime.toLocalTime();
        return !time.isBefore(PEAK_START) && time.isBefore(PEAK_END);
    }
}