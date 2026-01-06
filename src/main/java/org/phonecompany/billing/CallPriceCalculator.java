package org.phonecompany.billing;

import org.phonecompany.billing.model.Call;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

/**
 * Calculates the price for a single call based on time and duration.
 * Optimized version: iterates first 5 minutes, then calculates remaining minutes in batches.
 */
public class CallPriceCalculator {

    private static final LocalTime PEAK_START = LocalTime.of(8, 0, 0);
    private static final LocalTime PEAK_END = LocalTime.of(16, 0, 0);

    private static final BigDecimal PEAK_RATE = new BigDecimal("1.00");
    private static final BigDecimal OFF_PEAK_RATE = new BigDecimal("0.50");
    private static final BigDecimal LONG_CALL_DISCOUNT = new BigDecimal("0.20");

    private static final BigDecimal DISCOUNTED_PEAK_RATE = PEAK_RATE.subtract(LONG_CALL_DISCOUNT); // 0.80
    private static final BigDecimal DISCOUNTED_OFF_PEAK_RATE = OFF_PEAK_RATE.subtract(LONG_CALL_DISCOUNT); // 0.30

    private static final int STANDARD_MINUTES_THRESHOLD = 5;

    public BigDecimal calculate(Call call) {
        long totalMinutes = call.getDurationInMinutes();

        if (totalMinutes == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal total = BigDecimal.ZERO;

        // Step 1: Calculate first 5 minutes (or less) minute-by-minute
        long standardMinutes = Math.min(totalMinutes, STANDARD_MINUTES_THRESHOLD);
        for (int i = 0; i < standardMinutes; i++) {
            LocalDateTime minuteStart = call.getMinuteStartTime(i);
            total = total.add(getStandardRate(minuteStart));
        }

        // Step 2: Calculate remaining minutes in batches (with discount)
        if (totalMinutes > STANDARD_MINUTES_THRESHOLD) {
            long remainingMinutes = totalMinutes - STANDARD_MINUTES_THRESHOLD;
            LocalDateTime batchStart = call.startTime().plusMinutes(STANDARD_MINUTES_THRESHOLD);
            total = total.add(calculateDiscountedBatch(batchStart, remainingMinutes));
        }

        return total;
    }

    /**
     * Calculates discounted minutes in batches based on time intervals.
     * Much faster than minute-by-minute for long calls.
     */
    private BigDecimal calculateDiscountedBatch(LocalDateTime start, long minutes) {
        BigDecimal total = BigDecimal.ZERO;
        LocalDateTime current = start;
        long remaining = minutes;

        while (remaining > 0) {
            if (isPeakHour(current)) {
                // In peak hours - calculate until peak ends (16:00)
                long minutesUntilPeakEnd = ChronoUnit.MINUTES.between(current.toLocalTime(), PEAK_END);
                long batch = Math.min(remaining, minutesUntilPeakEnd);

                total = total.add(DISCOUNTED_PEAK_RATE.multiply(BigDecimal.valueOf(batch)));
                current = current.plusMinutes(batch);
                remaining -= batch;
            } else {
                // In off-peak hours - calculate until peak starts
                LocalTime currentTime = current.toLocalTime();
                long minutesUntilPeakStart;

                if (currentTime.isBefore(PEAK_START)) {
                    // Before 08:00 - calculate until 08:00 same day
                    minutesUntilPeakStart = ChronoUnit.MINUTES.between(currentTime, PEAK_START);
                } else {
                    // After 16:00 - calculate until 08:00 next day
                    // Minutes until end of day + minutes from midnight to 08:00
                    long minutesUntilMidnight = ChronoUnit.MINUTES.between(currentTime, LocalTime.of(23, 59)) + 1;
                    long minutesFromMidnightToPeak = ChronoUnit.MINUTES.between(LocalTime.MIDNIGHT, PEAK_START);
                    minutesUntilPeakStart = minutesUntilMidnight + minutesFromMidnightToPeak;
                }

                long batch = Math.min(remaining, minutesUntilPeakStart);

                total = total.add(DISCOUNTED_OFF_PEAK_RATE.multiply(BigDecimal.valueOf(batch)));
                current = current.plusMinutes(batch);
                remaining -= batch;
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