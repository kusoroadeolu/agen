package io.github.kusoroadeolu.agen.jmh;

import io.github.kusoradeolu.agen.expr.counters.ApproximateCounter;
import io.github.kusoradeolu.agen.expr.counters.DiminishedSynchronizedCounter;
import io.github.kusoradeolu.agen.expr.counters.SynchronizedCounter;
import io.github.kusoradeolu.agen.expr.counters.ThreadLocalCounter;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/*
Benchmark                                (counterType)   Mode  Cnt     Score     Error   Units
CounterBench.eightThreads                         sync  thrpt   75   880.309 ±  17.947  ops/us
CounterBench.eightThreads:add_8Threads            sync  thrpt   75     9.030 ±   0.353  ops/us
CounterBench.eightThreads:read_8Threads           sync  thrpt   75   871.279 ±  17.879  ops/us
CounterBench.eightThreads                     diminish  thrpt   74  1875.779 ± 566.177  ops/us
CounterBench.eightThreads:add_8Threads        diminish  thrpt   74  1086.268 ± 350.281  ops/us
CounterBench.eightThreads:read_8Threads       diminish  thrpt   74   789.511 ± 221.251  ops/us
CounterBench.eightThreads                       tlocal  thrpt   75   775.710 ± 360.378  ops/us
CounterBench.eightThreads:add_8Threads          tlocal  thrpt   75   723.930 ± 340.388  ops/us
CounterBench.eightThreads:read_8Threads         tlocal  thrpt   75    51.780 ±  20.015  ops/us
CounterBench.fourThreads                          sync  thrpt   75  2581.215 ± 372.951  ops/us
CounterBench.fourThreads:add_4Threads             sync  thrpt   75  1448.895 ± 287.283  ops/us
CounterBench.fourThreads:read_4Threads            sync  thrpt   75  1132.320 ±  85.988  ops/us
CounterBench.fourThreads                      diminish  thrpt   75   922.032 ± 439.219  ops/us
CounterBench.fourThreads:add_4Threads         diminish  thrpt   75   370.338 ± 292.086  ops/us
CounterBench.fourThreads:read_4Threads        diminish  thrpt   75   551.694 ± 158.211  ops/us
CounterBench.fourThreads                        tlocal  thrpt   75   783.016 ± 366.840  ops/us
CounterBench.fourThreads:add_4Threads           tlocal  thrpt   75   731.479 ± 346.397  ops/us
CounterBench.fourThreads:read_4Threads          tlocal  thrpt   75    51.537 ±  20.467  ops/us
CounterBench.twoThreads                           sync  thrpt   75  2350.921 ± 421.599  ops/us
CounterBench.twoThreads:add2Threads               sync  thrpt   75  1215.274 ± 263.443  ops/us
CounterBench.twoThreads:read2Threads              sync  thrpt   75  1135.647 ± 160.356  ops/us
CounterBench.twoThreads                       diminish  thrpt   75   724.157 ± 511.009  ops/us
CounterBench.twoThreads:add2Threads           diminish  thrpt   75   356.562 ± 282.240  ops/us
CounterBench.twoThreads:read2Threads          diminish  thrpt   75   367.595 ± 229.679  ops/us
CounterBench.twoThreads                         tlocal  thrpt   75  1097.987 ± 347.987  ops/us
CounterBench.twoThreads:add2Threads             tlocal  thrpt   75  1022.176 ± 326.265  ops/us
CounterBench.twoThreads:read2Threads            tlocal  thrpt   75    75.811 ±  21.726  ops/us
*
* */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Group)
@Warmup(iterations = 10, time = 1)
@Measurement(iterations = 7, time = 1)
@Fork(3)
public class CounterBench {
    @Param({"tlocal"})
    private String counterType;

    private ApproximateCounter counter;
    private static final long INCR = new Random().nextLong();

    @Setup(Level.Trial)
    public void setup(){
        this.counter = switch (counterType){
            case "sync" -> new SynchronizedCounter();
            case "diminish" -> new DiminishedSynchronizedCounter();
            case "tlocal" -> new ThreadLocalCounter();
            default -> null;
        };
    }

    @State(Scope.Thread)
    public static class ThreadState {
        long nextKey() {
            return INCR;
        }
    }

    @GroupThreads(2)
    @Group("twoThreads")
    @Benchmark
    public void add2Threads(Blackhole bh, ThreadState state){
        bh.consume(counter.add(state.nextKey()));
    }

    @GroupThreads(2)
    @Group("twoThreads")
    @Benchmark
    public void read2Threads(Blackhole bh){
        bh.consume(counter.sum());
    }


    @GroupThreads(4)
    @Group("fourThreads")
    @Benchmark
    public void add_4Threads(ThreadState state){
        counter.add(state.nextKey());
    }

    @GroupThreads(4)
    @Group("fourThreads")
    @Benchmark
    public void read_4Threads(Blackhole bh){
        bh.consume(counter.sum());
    }

    @GroupThreads(8)
    @Group("eightThreads")
    @Benchmark
    public void add_8Threads(ThreadState state){
        counter.add(state.nextKey());
    }

    @GroupThreads(8)
    @Group("eightThreads")
    @Benchmark
    public void read_8Threads(Blackhole bh){
        bh.consume(counter.sum());
    }


    public static class CounterRunner{
        void main() throws Exception {
            Options opt = new OptionsBuilder()
                    .include(CounterBench.class.getSimpleName())
                    .addProfiler("jfr", "dir=C:\\jfr-counter-output")
                    .build();
            new Runner(opt).run();
        }
    }
}
