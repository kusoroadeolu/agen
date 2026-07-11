package io.github.kusoroadeolu.agen.jmh;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 10, time = 1)
@Measurement(iterations = 10, time = 1)
@Fork(2)
@CompilerControl(CompilerControl.Mode.DONT_INLINE)
@State(Scope.Benchmark)
public class BranchPredictionBench {
    private static final int[] ARRAY = fill();

    @Param({"10", "30", "50", "70", "90"})
    private String threshold;

    @Benchmark
    public void bench(Blackhole bh) {
        int sum = 0;
        int b = Integer.parseInt(threshold);
        int[] a = ARRAY;
        int len = a.length;
        for (int i = 0; i < len; ++i) {
            int v = a[i];
            if (v > b) sum += v;
        }

        bh.consume(sum);
    }


    static int[] fill() {
        int len = 1 << 16;
        int[] array = new int[len];
        for (int i = 0; i < len; ++i){
            array[i] = ThreadLocalRandom.current().nextInt(100);
        }

//        Arrays.sort(array);
        return array;
    }

    public static class PipelineRunner{
        void main() throws Exception {
            Options opt = new OptionsBuilder()
                    .include(BranchPredictionBench.class.getSimpleName())
                    .build();
            new Runner(opt).run();
        }
    }

}

/* Unsorted
* Benchmark                    (threshold)   Mode  Cnt      Score     Error  Units
BranchPredictionBench.bench      10  thrpt   20  26221.576 ± 368.361  ops/s
BranchPredictionBench.bench      30  thrpt   20   8464.811 ±  90.114  ops/s
BranchPredictionBench.bench      50  thrpt   20   5498.072 ± 383.516  ops/s
BranchPredictionBench.bench      70  thrpt   20   7115.678 ± 127.504  ops/s
BranchPredictionBench.bench      90  thrpt   20  14666.053 ± 557.519  ops/s
* */

/* Sorted
Benchmark                    (threshold)   Mode  Cnt      Score      Error  Units
BranchPredictionBench.bench      10  thrpt   20  52431.893 ±  747.425  ops/s
BranchPredictionBench.bench      30  thrpt   20  45153.433 ± 1013.584  ops/s
BranchPredictionBench.bench      50  thrpt   20  39990.820 ±  883.996  ops/s
BranchPredictionBench.bench      70  thrpt   20  35132.726 ±  651.556  ops/s
BranchPredictionBench.bench      90  thrpt   20  32396.280 ±  641.441  ops/s
* */