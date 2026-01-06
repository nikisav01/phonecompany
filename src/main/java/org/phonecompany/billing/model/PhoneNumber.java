package org.phonecompany.billing.model;

/**
 * Represents a normalized phone number.
 * Immutable value object.
 */
public record PhoneNumber(String value) implements Comparable<PhoneNumber> {
    
    public PhoneNumber {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Phone number cannot be null or empty");
        }
        if (!value.matches("\\d+")) {
            throw new IllegalArgumentException("Phone number must contain only digits: " + value);
        }
    }
    
    /**
     * Compares phone numbers arithmetically.
     * Used for determining the highest-value number in promotions.
     */
    @Override
    public int compareTo(PhoneNumber other) {
        if (this.value.length() == other.value.length()) {
            return this.value.compareTo(other.value);
        }

        return Integer.compare(this.value.length(), other.value.length());
    }
    
    @Override
    public String toString() {
        return value;
    }
}