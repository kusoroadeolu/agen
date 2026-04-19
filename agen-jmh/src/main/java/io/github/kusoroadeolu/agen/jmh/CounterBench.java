package io.github.kusoroadeolu.agen.jmh;

import io.github.kusoradeolu.agen.expr.counters.ApproximateCounter;
import io.github.kusoradeolu.agen.expr.counters.DiminishedSynchronizedCounter;
import io.github.kusoradeolu.agen.expr.counters.SynchronizedCounter;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
public class CounterBench {
    @Param({"sync", "diminish"})
    private String counterType;

    private ApproximateCounter counter;

    @Setup(Level.Trial)
    public void setup(){
        this.counter = switch (counterType){
            case "sync" -> new SynchronizedCounter();
            case "diminish" -> new DiminishedSynchronizedCounter();
            default -> null;
        };
    }

    @State(Scope.Thread)
    public static class ThreadState {
        long incrBy = ThreadLocalRandom.current().nextLong();
        long nextKey() {
            return incrBy;
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
