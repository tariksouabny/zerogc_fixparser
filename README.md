# Zero-GC FIX Parser

[![](https://jitpack.io/v/tariksouabny/zerogc_fixparser.svg)](https://jitpack.io/#tariksouabny/zerogc_fixparser)

A Java parser for the Financial Information Exchange (FIX) protocol, built to explore how much parsing work can be done without allocating new objects.

The project focuses on a specific question: **can a parser read FIX fields, extract tags, and validate checksums while reusing the same memory across messages?**

## Motivation

FIX messages contain tag-value pairs separated by a delimiter. Converting each message into strings and splitting those strings is straightforward, but it also creates temporary objects.

This is a focused parser and benchmarking project. Avoiding allocations in the parser does not mean an entire trading application will be free of garbage-collection pauses.

## Implementation

The parser allocates its buffer and field objects when it is constructed, then reuses them for subsequent messages.

- **Reusable byte buffer.** Incoming message bytes are copied into an existing array.
- **Preallocated fields.** `FixField` objects are created during initialization and populated during parsing.
- **Integer arithmetic.** Numeric tags are read directly from ASCII bytes using operations such as `b - '0'`.
- **Offsets instead of strings.** Field values are represented by their position and length within the buffer, avoiding a separate string allocation for each value.

The main tradeoff is ownership: parsed fields refer to reusable storage. Callers must consume the values before the buffer and field objects are reused for another message. Preserving a value beyond that point requires copying it elsewhere.

## What I investigated

The project connects three parts of the problem:

1. **Message representation:** how to represent FIX fields without creating an object or string for every value.
2. **Memory reuse:** how to move allocation into initialization while keeping the parsing logic usable across repeated calls.
3. **Measurement:** how to benchmark the parsing path on the JVM and interpret the results without confusing throughput with latency.

The resulting design uses a fixed buffer and field capacity. Those capacities are chosen when the parser is constructed, so they need to match the messages the application expects to handle.

## Benchmark results

I benchmarked the parser using the Java Microbenchmark Harness (JMH).

The reported single-threaded run produced approximately **19.5 million parsing operations per second**:

```text
Benchmark                           Mode  Cnt         Score          Error  Units
FixParserBenchmark.testParser      thrpt    5  19552038.614 ±  2506563.591  ops/s
```

That throughput corresponds to approximately **51 ns per operation** when expressed as its reciprocal. It is not a separate latency measurement and does not describe tail latency or end-to-end network processing.

These results apply to the benchmark workload and test environment. Message size, field count, hardware, JVM configuration, and surrounding application work can all affect performance.

JMH supports JVM benchmarking, but the benchmark still needs to account for warmup, dead-code elimination, and other optimization effects. The throughput result alone also does not prove zero allocation; that requires allocation measurements.

## Usage

Construct the parser once so its buffer and field pool can be reused:

```java
// Capacity for a 1,024-byte message and 50 fields.
FixParser parser = new FixParser(1024, 50);
```

For each incoming message, copy its bytes into the parser's buffer and parse the message:

```java
// networkData contains the incoming FIX message.
// Its length must fit within the configured buffer capacity.
System.arraycopy(
    networkData, 0,
    parser.getBuffer(), 0,
    networkData.length
);

parser.parse(networkData.length);

if (parser.isValid()) {
    int fieldCount = parser.getFieldCount();

    for (int i = 0; i < fieldCount; i++) {
        FixField field = parser.getField(i);
        // Consume the field before parsing the next message.
    }
}
```

`getField(i)` accesses a field by its position in the parsed message, not by its FIX tag number.

The allocation-free goal applies to the parsing path. Creating input arrays, converting values to strings, or logging results may introduce allocations elsewhere.

## Building and testing

The project uses Maven and JUnit 5.

Run the unit tests:

```bash
mvn clean test
```

Compile and run the JMH benchmark:

```bash
mvn clean compile
mvn exec:java -Dexec.mainClass="org.example.FixParserBenchmark"
```

## Further evaluation

The next measurements would strengthen the conclusions beyond the initial throughput result:

- Allocation profiling to verify bytes allocated per parsing operation.
- Benchmarks across different message lengths and field counts.
- A string-based baseline tested under the same conditions.
- Separate latency measurements, including the distribution rather than only an average.
- Recorded hardware, JDK version, JVM options, and benchmark settings so results can be reproduced.
