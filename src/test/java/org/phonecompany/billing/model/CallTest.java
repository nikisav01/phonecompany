package org.phonecompany.billing.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CallTest {

    @Test
    void shouldThrowExceptionForNullPhoneNumber() {
        LocalDateTime start = LocalDateTime.of(2020, 1, 13, 10, 0, 0);
        LocalDateTime end = LocalDateTime.of(2020, 1, 13, 10, 5, 0);

        assertThrows(IllegalArgumentException.class, () -> new Call(null, start, end));
    }

    @Test
    void shouldThrowExceptionForNullStartTime() {
        PhoneNumber phoneNumber = new PhoneNumber("420774577453");
        LocalDateTime end = LocalDateTime.of(2020, 1, 13, 10, 5, 0);

        assertThrows(IllegalArgumentException.class, () -> new Call(phoneNumber, null, end));
    }

    @Test
    void shouldThrowExceptionForNullEndTime() {
        PhoneNumber phoneNumber = new PhoneNumber("420774577453");
        LocalDateTime start = LocalDateTime.of(2020, 1, 13, 10, 0, 0);

        assertThrows(IllegalArgumentException.class, () -> new Call(phoneNumber, start, null));
    }

    @Test
    void shouldThrowExceptionWhenEndTimeBeforeStartTime() {
        PhoneNumber phoneNumber = new PhoneNumber("420774577453");
        LocalDateTime start = LocalDateTime.of(2020, 1, 13, 10, 5, 0);
        LocalDateTime end = LocalDateTime.of(2020, 1, 13, 10, 0, 0);

        assertThrows(IllegalArgumentException.class, () -> new Call(phoneNumber, start, end));
    }

    @Test
    void shouldAllowEqualStartAndEndTime() {
        PhoneNumber phoneNumber = new PhoneNumber("420774577453");
        LocalDateTime time = LocalDateTime.of(2020, 1, 13, 10, 0, 0);

        Call call = new Call(phoneNumber, time, time);

        assertEquals(0, call.getDurationInMinutes());
    }

    @Test
    void shouldCalculateDurationInMinutesRoundedUp() {
        PhoneNumber phoneNumber = new PhoneNumber("420774577453");
        LocalDateTime start = LocalDateTime.of(2020, 1, 13, 10, 0, 0);
        LocalDateTime end = LocalDateTime.of(2020, 1, 13, 10, 2, 30);

        Call call = new Call(phoneNumber, start, end);

        assertEquals(3, call.getDurationInMinutes());
    }

    @Test
    void shouldRoundUpOneSecondToOneMinute() {
        PhoneNumber phoneNumber = new PhoneNumber("420774577453");
        LocalDateTime start = LocalDateTime.of(2020, 1, 13, 10, 0, 0);
        LocalDateTime end = LocalDateTime.of(2020, 1, 13, 10, 0, 1);

        Call call = new Call(phoneNumber, start, end);

        assertEquals(1, call.getDurationInMinutes());
    }

    @Test
    void shouldNotRoundUpExactMinutes() {
        PhoneNumber phoneNumber = new PhoneNumber("420774577453");
        LocalDateTime start = LocalDateTime.of(2020, 1, 13, 10, 0, 0);
        LocalDateTime end = LocalDateTime.of(2020, 1, 13, 10, 5, 0);

        Call call = new Call(phoneNumber, start, end);

        assertEquals(5, call.getDurationInMinutes());
    }

    @Test
    void shouldReturnCorrectMinuteStartTime() {
        PhoneNumber phoneNumber = new PhoneNumber("420774577453");
        LocalDateTime start = LocalDateTime.of(2020, 1, 13, 10, 0, 0);
        LocalDateTime end = LocalDateTime.of(2020, 1, 13, 10, 5, 0);

        Call call = new Call(phoneNumber, start, end);

        assertEquals(LocalDateTime.of(2020, 1, 13, 10, 0, 0), call.getMinuteStartTime(0));
        assertEquals(LocalDateTime.of(2020, 1, 13, 10, 1, 0), call.getMinuteStartTime(1));
        assertEquals(LocalDateTime.of(2020, 1, 13, 10, 4, 0), call.getMinuteStartTime(4));
    }

    @Test
    void shouldThrowExceptionForNegativeMinuteIndex() {
        PhoneNumber phoneNumber = new PhoneNumber("420774577453");
        LocalDateTime start = LocalDateTime.of(2020, 1, 13, 10, 0, 0);
        LocalDateTime end = LocalDateTime.of(2020, 1, 13, 10, 5, 0);

        Call call = new Call(phoneNumber, start, end);

        assertThrows(IllegalArgumentException.class, () -> call.getMinuteStartTime(-1));
    }

    @Test
    void shouldThrowExceptionForMinuteIndexOutOfBounds() {
        PhoneNumber phoneNumber = new PhoneNumber("420774577453");
        LocalDateTime start = LocalDateTime.of(2020, 1, 13, 10, 0, 0);
        LocalDateTime end = LocalDateTime.of(2020, 1, 13, 10, 5, 0);

        Call call = new Call(phoneNumber, start, end);

        assertThrows(IllegalArgumentException.class, () -> call.getMinuteStartTime(5));
    }
}