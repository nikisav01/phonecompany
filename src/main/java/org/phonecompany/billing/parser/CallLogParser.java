package org.phonecompany.billing.parser;

import org.phonecompany.billing.model.Call;
import java.util.List;

/**
 * Interface for parsing phone call logs.
 */
public interface CallLogParser {
    
    /**
     * Parses a phone log string and returns a list of calls.
     * 
     * @param phoneLog the phone log in string format
     * @return list of parsed calls
     * @throws IllegalArgumentException if the log format is invalid
     */
    List<Call> parse(String phoneLog);
}