package io.github.kusoroadeolu.agen.jmh;

import io.github.kusoradeolu.agen.expr.fc.SequencerFlatCombiner;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 10, time = 1)
@Measurement(iterations = 5, time = 1)
public class CombinerBench {
    private SequencerFlatCombiner<?> combiner;

    @Setup
    public void setup(){
        combiner = new SequencerFlatCombiner<>(0);
    }


    @Threads(2)
    @Benchmark
    public void twoThreads(Blackhole bh){
        bh.consume(combiner.combine(e -> Blackhole.consumeCPU(100)));
    }


    @Threads(4)
    @Benchmark
    public void fourThreads(Blackhole bh){
        bh.consume(combiner.combine(e -> Blackhole.consumeCPU(100)));
    }


    @Threads(8)
    @Benchmark
    public void eightThreads(Blackhole bh){
       bh.consume(combiner.combine(e -> Blackhole.consumeCPU(100)));
    }


    public static class CounterRunner{
        void main() throws Exception {
            Options opt = new OptionsBuilder()
                    .include(CombinerBench.class.getSimpleName())
                    .build();
            new Runner(opt).run();
        }
    }
}
