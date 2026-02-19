package org.example;

import java.nio.charset.StandardCharsets;

public class Main {
    public static void main(String[] args) {
        System.out.println("Starting ZeroGC FIX Parser: ");

        FixParser parser = new FixParser(1024, 50);

        String rawFix = "8=FIX.4.2\u000135=D\u000155=AAPL\u000154=1\u000110=065\u0001";
        byte[] inputBytes = rawFix.getBytes(StandardCharsets.US_ASCII);

        System.arraycopy(inputBytes, 0, parser.getBuffer(), 0, inputBytes.length);
        
        parser.parse(inputBytes.length);
        
        System.out.println("Parsed " + parser.getFieldCount() + " fields.");
        System.out.println("Checksum Valid? " + parser.isValid());

        String badFix = "8=FIX.4.2\u000135=D\u000155=GOOG\u000154=1\u000110=065\u0001";
        byte[] badBytes = badFix.getBytes(StandardCharsets.US_ASCII);
        
        System.arraycopy(badBytes, 0, parser.getBuffer(), 0, badBytes.length);
        parser.parse(badBytes.length);
        
        System.out.println("--------------------------------");
        System.out.println("Parsed Corrupt Message.");
        System.out.println("Checksum Valid? " + parser.isValid());
    }
}