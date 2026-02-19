package org.example;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput) // Measures operations (parses) per second
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 3, time = 1) // Warms up the JVM to trigger JIT compilation
@Measurement(iterations = 5, time = 1) // The actual recorded test
@Fork(0) // do not change to 1
public class FixParserBenchmark {

    private FixParser parser;
    private byte[] messageBytes;

    @Setup
    public void setup() {
        // Initialize everything ONCE before the timer starts
        parser = new FixParser(1024, 50);
        String rawFix = "8=FIX.4.2\u000135=D\u000155=AAPL\u000154=1\u000110=167\u0001";
        messageBytes = rawFix.getBytes(StandardCharsets.US_ASCII);
        
        // Pre-load the buffer
        System.arraycopy(messageBytes, 0, parser.getBuffer(), 0, messageBytes.length);
    }

    @Benchmark
    public int testParser() {
        // This is the ONLY code that gets timed
        parser.parse(messageBytes.length);
        
        // We return a value so the JVM doesn't "dead-code eliminate" our method
        return parser.getFieldCount(); 
    }

    // A handy main method to run the benchmark directly
    public static void main(String[] args) throws Exception {
        Options opt = new OptionsBuilder()
                .include(FixParserBenchmark.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}