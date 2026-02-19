package org.example;

import java.nio.charset.StandardCharsets;
/* 
    Using FixField to avoid memory allocation
    with Strings.
*/
public class FixField {
    public int tag;
    public int valueOffset;
    public int valueLength;

    // <helper> method
    public String getValue(byte[] rawData) {
        return new String(rawData, valueOffset, valueLength, StandardCharsets.US_ASCII);
    }
}