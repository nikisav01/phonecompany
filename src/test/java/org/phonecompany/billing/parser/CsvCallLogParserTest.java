package org.phonecompany.billing.parser;

import org.phonecompany.billing.model.Call;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvCallLogParserTest {

    private final CsvCallLogParser parser = new CsvCallLogParser();

    @Test
    void shouldParseValidSingleLine() {
        String log = "420774577453,13-01-2020 18:10:15,13-01-2020 18:12:57";

        List<Call> calls = parser.parse(log);

        assertEquals(1, calls.size());
        assertEquals("420774577453", calls.getFirst().phoneNumber().value());
    }

    @Test
    void shouldParseMultipleLines() {
        String log = """
            420774577453,13-01-2020 18:10:15,13-01-2020 18:12:57
            420776562353,18-01-2020 08:59:20,18-01-2020 09:10:00
            """;

        List<Call> calls = parser.parse(log);

        assertEquals(2, calls.size());
    }

    @Test
    void shouldReturnEmptyListForEmptyLog() {
        List<Call> calls = parser.parse("");

        assertTrue(calls.isEmpty());
    }

    @Test
    void shouldReturnEmptyListForNullLog() {
        List<Call> calls = parser.parse(null);

        assertTrue(calls.isEmpty());
    }

    @Test
    void shouldSkipEmptyLines() {
        String log = """
            420774577453,13-01-2020 18:10:15,13-01-2020 18:12:57
            
            420776562353,18-01-2020 08:59:20,18-01-2020 09:10:00
            """;

        List<Call> calls = parser.parse(log);

        assertEquals(2, calls.size());
    }

    @Test
    void shouldThrowExceptionForInvalidFieldCount() {
        String log = "420774577453,13-01-2020 18:10:15";

        assertThrows(IllegalArgumentException.class, () -> parser.parse(log));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "420774577453,13/01/2020 18:10:15,13-01-2020 18:12:57",
            "420774577453,13-01-2020 18:10,13-01-2020 18:12:57",
            "420774577453,invalid,13-01-2020 18:12:57"
    })
    void shouldThrowExceptionForInvalidDateFormat(String log) {
        assertThrows(IllegalArgumentException.class, () -> parser.parse(log));
    }

    @Test
    void shouldThrowExceptionForInvalidPhoneNumber() {
        String log = "42077abc7453,13-01-2020 18:10:15,13-01-2020 18:12:57";

        assertThrows(IllegalArgumentException.class, () -> parser.parse(log));
    }

    @Test
    void shouldHandleWhitespaceInFields() {
        String log = " 420774577453 , 13-01-2020 18:10:15 , 13-01-2020 18:12:57 ";

        List<Call> calls = parser.parse(log);

        assertEquals(1, calls.size());
    }
}