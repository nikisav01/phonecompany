package org.phonecompany.billing;

import org.phonecompany.billing.model.Call;
import org.phonecompany.billing.model.PhoneNumber;
import org.phonecompany.billing.parser.CallLogParser;
import org.phonecompany.billing.parser.CsvCallLogParser;
import org.phonecompany.billing.promotion.FreePromotionStrategy;
import org.phonecompany.billing.promotion.MostCalledNumberPromotion;

import java.math.BigDecimal;
import java.util.List;

/**
 * Main implementation of the telephone bill calculator.
 * Applies the following rules:
 * - Peak hours [08:00-16:00): 1.00 Kč per minute
 * - Off-peak hours: 0.50 Kč per minute
 * - Calls longer than 5 minutes: 0.20 Kč discount per minute after the 5th
 * - Promotion: calls to the most frequently called number are free
 */
public class TelephoneBillCalculatorImpl implements TelephoneBillCalculator {

    private final CallLogParser parser;
    private final CallPriceCalculator priceCalculator;
    private final FreePromotionStrategy promotionStrategy;

    public TelephoneBillCalculatorImpl(
            CallLogParser parser,
            CallPriceCalculator priceCalculator,
            FreePromotionStrategy promotionStrategy) {
        this.parser = parser;
        this.priceCalculator = priceCalculator;
        this.promotionStrategy = promotionStrategy;
    }

    public TelephoneBillCalculatorImpl() {
        this(
                new CsvCallLogParser(),
                new CallPriceCalculator(),
                new MostCalledNumberPromotion()
        );
    }

    @Override
    public BigDecimal calculate(String phoneLog) {
        // 1. Parse the phone log
        List<Call> allCalls = parser.parse(phoneLog);

        if (allCalls.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // 2. Apply promotion to find free phone number
        PhoneNumber freeNumber = promotionStrategy.getFreePhoneNumber(allCalls).orElse(null);

        // 3. Calculate total price, excluding calls to free number
        return calculateTotalPrice(allCalls, freeNumber);
    }

    /**
     * Calculates the total price for all billable calls.
     * Calls to the free number (if not null) are excluded.
     *
     * @param freeNumber phone number to exclude, or null if no promotion applies
     */
    private BigDecimal calculateTotalPrice(List<Call> calls, PhoneNumber freeNumber) {
        return calls.stream()
                .filter(call -> isBillable(call, freeNumber))
                .map(priceCalculator::calculate)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Determines if a call should be billed.
     * Returns false if the call is to the free promotional number.
     */
    private boolean isBillable(Call call, PhoneNumber freeNumber) {
        return !call.phoneNumber().equals(freeNumber);
    }
}