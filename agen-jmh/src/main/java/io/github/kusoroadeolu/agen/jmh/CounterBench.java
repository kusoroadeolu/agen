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
* Benchmark                                (counterType)   Mode  Cnt     Score     Error   Units
CounterBench.eightThreads                         sync  thrpt   35  1266.608 ± 520.763  ops/us
CounterBench.eightThreads:add_8Threads            sync  thrpt   35   358.721 ± 432.198  ops/us
CounterBench.eightThreads:read_8Threads           sync  thrpt   35   907.887 ±  97.212  ops/us
CounterBench.eightThreads                     diminish  thrpt   35  1876.948 ± 800.931  ops/us
CounterBench.eightThreads:add_8Threads        diminish  thrpt   35  1044.361 ± 525.641  ops/us
CounterBench.eightThreads:read_8Threads       diminish  thrpt   35   832.587 ± 280.047  ops/us
CounterBench.eightThreads                       tlocal  thrpt   35  1121.424 ± 539.821  ops/us
CounterBench.eightThreads:add_8Threads          tlocal  thrpt   35  1043.083 ± 515.327  ops/us
CounterBench.eightThreads:read_8Threads         tlocal  thrpt   35    78.342 ±  25.047  ops/us
CounterBench.fourThreads                          sync  thrpt   35  1055.854 ± 596.295  ops/us
CounterBench.fourThreads:add_4Threads             sync  thrpt   35   360.597 ± 435.492  ops/us
CounterBench.fourThreads:read_4Threads            sync  thrpt   35   695.257 ± 161.242  ops/us
CounterBench.fourThreads                      diminish  thrpt   35   746.049 ± 696.561  ops/us
CounterBench.fourThreads:add_4Threads         diminish  thrpt   35   357.307 ± 438.114  ops/us
CounterBench.fourThreads:read_4Threads        diminish  thrpt   35   388.743 ± 263.619  ops/us
CounterBench.fourThreads                        tlocal  thrpt   35   386.647 ± 438.252  ops/us
CounterBench.fourThreads:add_4Threads           tlocal  thrpt   35   357.087 ± 414.054  ops/us
CounterBench.fourThreads:read_4Threads          tlocal  thrpt   35    29.560 ±  24.219  ops/us
CounterBench.twoThreads                           sync  thrpt   35  1091.835 ± 534.731  ops/us
CounterBench.twoThreads:add2Threads               sync  thrpt   35   456.928 ± 340.770  ops/us
CounterBench.twoThreads:read2Threads              sync  thrpt   35   634.908 ± 200.738  ops/us
CounterBench.twoThreads                       diminish  thrpt   35   901.151 ± 607.584  ops/us
CounterBench.twoThreads:add2Threads           diminish  thrpt   35   449.534 ± 338.801  ops/us
CounterBench.twoThreads:read2Threads          diminish  thrpt   35   451.617 ± 271.690  ops/us
CounterBench.twoThreads                         tlocal  thrpt   35    25.438 ±   0.679  ops/us
CounterBench.twoThreads:add2Threads             tlocal  thrpt   35    17.047 ±   0.392  ops/us
CounterBench.twoThreads:read2Threads            tlocal  thrpt   35     8.391 ±   0.446  ops/us
*
* */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Group)
@Warmup(iterations = 10, time = 1)
@Measurement(iterations = 7, time = 1)
public class CounterBench {
    @Param({"sync", "diminish", "tlocal"})
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
                    .build();
            new Runner(opt).run();
        }
    }
}
