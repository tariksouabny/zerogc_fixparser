# Zero-GC FIX Parser

A high-performance, zero-allocation Financial Information eXchange (FIX) protocol parser written in Java. Designed for low-latency trading systems where garbage collection (GC) pauses are unacceptable.

## 🚀 The Problem: Garbage Collection in HFT
In High-Frequency Trading (HFT), milliseconds can cost millions. Standard string manipulation in Java (like `String.split()` or `new String(bytes)`) creates massive amounts of temporary objects. This puts immense pressure on the Java Garbage Collector, eventually triggering "Stop-The-World" pauses that freeze the application and cause missed trading opportunities.

## 💡 The Solution: Zero Allocation
This parser takes a radically different approach: **Zero Object Allocation during runtime.**
* **Pre-allocated Buffers:** All byte data is read directly into a pre-allocated flat byte array.
* **Object Pooling:** `FixField` objects are instantiated exactly once at startup.
* **Primitive Math:** Tags and checksums are calculated using primitive integer math (`b - '0'`) instead of string parsing.
* **No Strings Attached:** Values are extracted via offsets and lengths directly from the raw byte buffer.

Once the parser is warmed up, parsing a FIX message creates **0 new objects**, meaning the Garbage Collector never has to run.

## 📊 Performance & JMH Benchmarks
This project uses the Java Microbenchmark Harness (JMH) to prevent JVM optimization cheating and measure raw throughput. 

**Results:** ~19.5 Million messages parsed per second.
**Latency:** ~51 nanoseconds per parse.

```text
Benchmark                           Mode  Cnt         Score          Error  Units
FixParserBenchmark.testParser      thrpt    5  19552038.614 ±  2506563.591  ops/s
```
*(Tested on standard hardware. Single-threaded throughput).*

## 🛠️ Usage
Initialize the parser once during your application's startup phase to pre-allocate memory.

```java
// Initialize with a 1024-byte buffer and a pool of 50 reusable fields
FixParser parser = new FixParser(1024, 50);

// Receive raw ASCII bytes from your network socket
byte[] networkData = "8=FIX.4.2\u000135=D\u000155=AAPL\u000154=1\u000110=167\u0001".getBytes(StandardCharsets.US_ASCII);

// Load data into the parser's internal buffer (avoids object creation)
System.arraycopy(networkData, 0, parser.getBuffer(), 0, networkData.length);

// Parse the message
parser.parse(networkData.length);

// Validate and read
if (parser.isValid()) {
    System.out.println("Message contains " + parser.getFieldCount() + " fields.");
    FixField msgType = parser.getField(1); // Get Tag 35 (MsgType)
    System.out.println("Tag: " + msgType.tag);
}
```

## 🏗️ Building and Testing
This project uses standard Maven build lifecycles.

**Run the Unit Tests (JUnit 5):**
```bash
mvn clean test
```

**Run the JMH Benchmarks:**
```bash
mvn clean compile
mvn exec:java -Dexec.mainClass="org.example.FixParserBenchmark"
```
