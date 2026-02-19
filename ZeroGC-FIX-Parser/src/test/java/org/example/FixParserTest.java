package org.example;

import org.junit.jupiter.api.Test;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;

public class FixParserTest {

    @Test
    public void testValidMessage() {
        FixParser parser = new FixParser(1024, 50);
        // Corrected Checksum to 167
        String rawFix = "8=FIX.4.2\u000135=D\u000155=AAPL\u000154=1\u000110=167\u0001";
        byte[] data = rawFix.getBytes(StandardCharsets.US_ASCII);

        System.arraycopy(data, 0, parser.getBuffer(), 0, data.length);
        parser.parse(data.length);

        assertEquals(5, parser.getFieldCount(), "Should parse 5 fields");
        assertTrue(parser.isValid(), "Checksum should be valid");
        
        FixField msgType = parser.getField(1);
        assertEquals(35, msgType.tag);
        assertEquals("D", msgType.getValue(parser.getBuffer()));
    }

    @Test
    public void testCorruptMessage() {
        FixParser parser = new FixParser(1024, 50);
        // AAPL changed to GOOG, which breaks the 167 checksum
        String badFix = "8=FIX.4.2\u000135=D\u000155=GOOG\u000154=1\u000110=167\u0001";
        byte[] data = badFix.getBytes(StandardCharsets.US_ASCII);

        System.arraycopy(data, 0, parser.getBuffer(), 0, data.length);
        parser.parse(data.length);

        assertFalse(parser.isValid(), "Checksum should FAIL for corrupted data");
    }
}