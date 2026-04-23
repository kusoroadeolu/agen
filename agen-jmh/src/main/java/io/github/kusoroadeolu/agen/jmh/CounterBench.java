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
