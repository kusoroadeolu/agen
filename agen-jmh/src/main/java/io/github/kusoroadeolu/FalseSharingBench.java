package io.github.kusoroadeolu;

import io.github.kusoroadeolu.mem.falsesharing.Accum;
import io.github.kusoroadeolu.mem.falsesharing.Counter;
import io.github.kusoroadeolu.mem.falsesharing.PaddedCounter;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/*

Counter no padding
* OFF  SZ   TYPE DESCRIPTION               VALUE
  0   8        (object header: mark)     0x0000000000000001 (non-biasable; age: 0)
  8   4        (object header: class)    0x01042a10
 12   4    int Counter.a                 0
 16   4    int Counter.b                 0
 20   4        (object alignment gap)
Instance size: 24 bytes

Counter padded
OFF  SZ   TYPE DESCRIPTION               VALUE
  0   8        (object header: mark)     0x0000000000000001 (non-biasable; age: 0)
  8   4        (object header: class)    0x01042a10
 12   4    int PaddedCounter.a           0
 16   8   long PaddedCounter.l1          0
 24   8   long PaddedCounter.l2          0
 32   8   long PaddedCounter.l3          0
 40   8   long PaddedCounter.l4          0
 48   8   long PaddedCounter.l5          0
 56   8   long PaddedCounter.l6          0
 64   8   long PaddedCounter.l7          0
 72   8   long PaddedCounter.l8          0
 80   4    int PaddedCounter.b           0
 84   4        (object alignment gap)
Instance size: 88 bytes
Space losses: 0 bytes internal + 4 bytes external = 4 bytes total


Bench Results. Padded counter outperforms by almost 50%
Benchmark                      (impl)   Mode  Cnt         Score         Error  Units
FalseSharingBench.q           COUNTER  thrpt   21  29831274.577 ± 6431467.958  ops/s
FalseSharingBench.q:a         COUNTER  thrpt   21  15738622.692 ± 4727742.728  ops/s
FalseSharingBench.q:b         COUNTER  thrpt   21  14092651.884 ± 2193883.084  ops/s
FalseSharingBench.q    PADDED_COUNTER  thrpt   21  43825153.846 ± 7025444.558  ops/s
FalseSharingBench.q:a  PADDED_COUNTER  thrpt   21  25560916.838 ± 3509834.789  ops/s
FalseSharingBench.q:b  PADDED_COUNTER  thrpt   21  18264237.009 ± 4296462.745  ops/s
*
* */

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 7, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(value = 3)
@State(Scope.Group)
public class FalseSharingBench {


    @Param({"COUNTER", "PADDED_COUNTER"})
    String impl;

    static final Integer ITEM_A = 1;
    static final Integer ITEM_B = 2;
    Accum q;
    @Setup(Level.Iteration)
    public void setup() throws Exception {
        q = impl.equals("COUNTER")
                ? new Counter()
                : new PaddedCounter();
    }
    @Benchmark
    @Group("q")
    @GroupThreads()
    public void a(Blackhole bh) {
        bh.consume(q.setA(ITEM_A));
    }
    @Benchmark
    @Group("q")
    @GroupThreads()
    public void b(Blackhole bh) {
        bh.consume(q.setB(ITEM_B));
    }


    static class Runner {
        void main() throws RunnerException {
            Options opt = new OptionsBuilder()
                        .include(FalseSharingBench.class.getSimpleName())
                        .build();
            new org.openjdk.jmh.runner.Runner(opt).run();
        }
    }

}
