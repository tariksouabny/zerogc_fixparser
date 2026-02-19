package org.example;

public class FixParser {
    private final byte[] dataBuffer;
    private final FixField[] fieldPool;
    private int fieldCount = 0;
    private boolean isChecksumValid = false;

    public FixParser(int bufferSize, int maxFields) {
        this.dataBuffer = new byte[bufferSize];
        this.fieldPool = new FixField[maxFields];

        for (int i = 0; i < maxFields; i++) {
            this.fieldPool[i] = new FixField();
        }
    }

    public byte[] getBuffer() { return dataBuffer; }
    public int getFieldCount() { return fieldCount; }
    public boolean isValid() { return isChecksumValid; }
    
    public FixField getField(int index) {
        return fieldPool[index];
    }

    public void parse(int length) {
        fieldCount = 0;
        int currentTag = 0;
        int valueStart = -1;
        boolean parsingTag = true;
        
        int totalByteSum = 0; 
        int calculatedChecksum = 0;
        isChecksumValid = false;

        for(int i = 0; i < length; i++) {
            byte b = dataBuffer[i];
            
            totalByteSum += b;

            if(parsingTag) {
                if(b == FixConstants.EQUALS) {
                    parsingTag = false;
                    valueStart = i + 1;
                    
                    if (currentTag == FixConstants.TAG_CHECKSUM) {
                        // BUG FIXED: We only subtract '1', '0', and '=' once.
                        int correction = '1' + '0' + '=';
                        calculatedChecksum = (totalByteSum - correction) % 256;
                    }
                }
                else {
                    currentTag = (currentTag * 10) + (b - '0');
                }
            }
            else {
                if(b == FixConstants.SOH) {
                    FixField field = fieldPool[fieldCount++];
                    field.tag = currentTag;
                    field.valueOffset = valueStart;
                    field.valueLength = i - valueStart;
                    
                    if (currentTag == FixConstants.TAG_CHECKSUM) {
                        int messageChecksum = parseChecksumValue(valueStart, i - valueStart);
                        if (messageChecksum == calculatedChecksum) {
                            isChecksumValid = true;
                        }
                    }

                    parsingTag = true;
                    currentTag = 0;
                }
            }
        }
    }

    private int parseChecksumValue(int offset, int length) {
        int sum = 0;
        for (int i = 0; i < length; i++) {
            sum = (sum * 10) + (dataBuffer[offset + i] - '0');
        }
        return sum;
    }
}