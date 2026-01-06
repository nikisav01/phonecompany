package org.phonecompany.billing.promotion;

import org.phonecompany.billing.model.Call;
import org.phonecompany.billing.model.PhoneNumber;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Promotion that makes calls to the most frequently called number free.
 */
public class MostCalledNumberPromotion implements FreePromotionStrategy {
    
    @Override
    public Optional<PhoneNumber> getFreePhoneNumber(List<Call> calls) {
        if (calls == null || calls.isEmpty()) {
            return Optional.empty();
        }
        
        Map<PhoneNumber, Long> callCounts = countCallsByPhoneNumber(calls);
        
        if (callCounts.isEmpty()) {
            return Optional.empty();
        }
        
        long maxCount = findMaxCallCount(callCounts);
        
        return findHighestPhoneNumberWithCount(callCounts, maxCount);
    }
    
    /**
     * Counts the number of calls for each phone number.
     */
    private Map<PhoneNumber, Long> countCallsByPhoneNumber(List<Call> calls) {
        return calls.stream()
            .collect(Collectors.groupingBy(
                Call::phoneNumber,
                Collectors.counting()
            ));
    }
    
    /**
     * Finds the maximum number of calls made to any single number.
     */
    private long findMaxCallCount(Map<PhoneNumber, Long> callCounts) {
        return callCounts.values().stream()
            .mapToLong(Long::longValue)
            .max()
            .orElse(0L);
    }
    
    /**
     * Finds the arithmetically highest phone number among those with the given call count.
     */
    private Optional<PhoneNumber> findHighestPhoneNumberWithCount(
            Map<PhoneNumber, Long> callCounts, 
            long targetCount) {
        
        return callCounts.entrySet().stream()
            .filter(entry -> entry.getValue() == targetCount)
            .map(Map.Entry::getKey)
            .max(PhoneNumber::compareTo);
    }
}