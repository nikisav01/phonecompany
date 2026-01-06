package org.phonecompany.billing.parser;

import org.phonecompany.billing.model.Call;
import org.phonecompany.billing.model.PhoneNumber;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses phone call logs in CSV format.
 * Expected format: phoneNumber,startTime,endTime
 * Date format: dd-MM-yyyy HH:mm:ss
 */
public class CsvCallLogParser implements CallLogParser {
    
    private static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    
    private static final String CSV_DELIMITER = ",";
    private static final int EXPECTED_FIELDS = 3;
    
    @Override
    public List<Call> parse(String phoneLog) {
        if (phoneLog == null || phoneLog.isBlank()) {
            return List.of();
        }
        
        List<Call> calls = new ArrayList<>();
        String[] lines = phoneLog.split("\\r?\\n");
        
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            
            if (line.isEmpty()) {
                continue;
            }
            
            try {
                Call call = parseLine(line);
                calls.add(call);
            } catch (Exception e) {
                throw new IllegalArgumentException(
                    "Failed to parse line " + (i + 1) + ": " + line, e
                );
            }
        }
        
        return calls;
    }
    
    private Call parseLine(String line) {
        String[] fields = line.split(CSV_DELIMITER);
        
        if (fields.length != EXPECTED_FIELDS) {
            throw new IllegalArgumentException(
                "Expected " + EXPECTED_FIELDS + " fields, but got " + fields.length
            );
        }
        
        String phoneNumberStr = fields[0].trim();
        String startTimeStr = fields[1].trim();
        String endTimeStr = fields[2].trim();
        
        PhoneNumber phoneNumber = new PhoneNumber(phoneNumberStr);
        LocalDateTime startTime = parseDateTime(startTimeStr);
        LocalDateTime endTime = parseDateTime(endTimeStr);
        
        return new Call(phoneNumber, startTime, endTime);
    }
    
    private LocalDateTime parseDateTime(String dateTimeStr) {
        try {
            return LocalDateTime.parse(dateTimeStr, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                "Invalid date format: " + dateTimeStr + 
                ". Expected format: dd-MM-yyyy HH:mm:ss", e
            );
        }
    }
}