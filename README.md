# Zero-GC FIX Parser
[![](https://jitpack.io/v/tariksouabny/zerogc_fixparser.svg)](https://jitpack.io/#tariksouabny/zerogc_fixparser)

Zero-allocation Financial Information Exchange \FIX\ protocol parser written in Java. Designed for low-latency trading systems where garbage collection pauses CANNOT occur.

## Design
In HFT, standard string manipulation in Java using `String.split()` or `new String(bytes)` creates a comparably massive amount of temp objects- which is more data through the Java Garbage Collector, which triggers stop-pauses.

As such, I designed a parser that uses **zero object allocation during runtime.** i.e., some of the features I've used were:
* Pre-allocated Buffers: All byte data is read directly into a pre-alloc. byte array.
* Pooling Objects: `FixField` objs are instantiated exactly once at startup.
* Primitive Math: Tags and checksums are calculated using primitive integer math (`b - '0'`) instead of str parsing.
* No Strings Attached; values are extracted with offsets and lengths from the raw byte buffer.

So when the parser is primed, parsing a FIX message creates 0 new objects. 

## Results
My project uses the Java Microbenchmark Harness (JMH) to stop JVM optimisation cheating and measure throughput. 

**Results:** 19.5m messages parsed/sec.
**Latency:** 51 nanoseconds/parse.

```text
Benchmark                           Mode  Cnt         Score          Error  Units
FixParserBenchmark.testParser      thrpt    5  19552038.614 ±  2506563.591  ops/s
```
This is tested on standard hardware w/ single-threaded throughput

## Usage
To use this, you can initialize the parser once as you start up your book/application to pre-allocate memory.

```java
//  1024-byte buffer, pool of 50 fields
FixParser parser = new FixParser(1024, 50);
byte[] networkData = "8=FIX.4.2\u000135=D\u000155=AAPL\u000154=1\u000110=167\u0001".getBytes(StandardCharsets.US_ASCII);
System.arraycopy(networkData, 0, parser.getBuffer(), 0, networkData.length);
parser.parse(networkData.length);
if (parser.isValid()) {
    System.out.println("Message contains " + parser.getFieldCount() + " fields.");
    FixField msgType = parser.getField(1); // Get Tag 35 (MsgType)
    System.out.println("Tag: " + msgType.tag);
}
```

## Building and Testing
This project uses Maven build/lifecycles.

**Run the unit tests, JUnit 5:**
```bash
mvn clean test
```

**Run the JMH Benchmarks:**
```bash
mvn clean compile
mvn exec:java -Dexec.mainClass="org.example.FixParserBenchmark"
```
