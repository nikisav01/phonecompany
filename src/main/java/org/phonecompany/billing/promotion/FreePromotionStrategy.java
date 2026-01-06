package org.phonecompany.billing.promotion;

import org.phonecompany.billing.model.Call;
import org.phonecompany.billing.model.PhoneNumber;

import java.util.List;
import java.util.Optional;

/**
 * Strategy for determining which phone numbers should be excluded from billing
 * due to promotional offers.
 */
public interface FreePromotionStrategy {
    
    /**
     * Determines which phone number should be free based on the promotion rules.
     * 
     * @param calls list of all calls
     * @return phone number that should be free, or empty if no promotion applies
     */
    Optional<PhoneNumber> getFreePhoneNumber(List<Call> calls);
}