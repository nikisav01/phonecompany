package org.phonecompany.billing.model;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Represents a single phone call.
 * Immutable value object.
 */
public record Call(
        PhoneNumber phoneNumber,
        LocalDateTime startTime,
        LocalDateTime endTime
) {

    public Call {
        if (phoneNumber == null) {
            throw new IllegalArgumentException("Phone number cannot be null");
        }
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("Start time and end time cannot be null");
        }
        if (endTime.isBefore(startTime)) {
            throw new IllegalArgumentException("End time cannot be before start time");
        }
    }

    /**
     * Calculates the duration of the call in minutes (rounded up).
     */
    public long getDurationInMinutes() {
        long seconds = ChronoUnit.SECONDS.between(startTime, endTime);

        return (seconds + 59) / 60;
    }

    /**
     * Returns the start time of a specific minute of the call (0-indexed).
     * Used to determine which rate applies to each minute.
     */
    public LocalDateTime getMinuteStartTime(int minuteIndex) {
        if (minuteIndex < 0 || minuteIndex >= getDurationInMinutes()) {
            throw new IllegalArgumentException("Invalid minute index: " + minuteIndex);
        }
        return startTime.plusMinutes(minuteIndex);
    }
}