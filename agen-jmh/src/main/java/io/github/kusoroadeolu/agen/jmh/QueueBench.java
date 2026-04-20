package io.github.kusoroadeolu.agen.jmh;

import io.github.kusoradeolu.agen.expr.queues.SPSCQueue;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Queue;
import java.util.concurrent.TimeUnit;

//Base line bench, ABQ
/*
Benchmark                          Mode  Cnt   Score   Error   Units
QueueBench.oneThread              thrpt   45  22.074 ± 0.556  ops/us
QueueBench.oneThread:add1Thread   thrpt   45  11.604 ± 1.396  ops/us
QueueBench.oneThread:read1Thread  thrpt   45  10.471 ± 1.228  ops/us
* */


//With varhandles and set release
/*
Benchmark                          Mode  Cnt    Score   Error   Units
QueueBench.oneThread              thrpt   45  104.285 ± 4.445  ops/us
QueueBench.oneThread:add1Thread   thrpt   45   43.668 ± 2.285  ops/us
QueueBench.oneThread:read1Thread  thrpt   45   60.617 ± 2.318  ops/us
*/
/*
* Benchmark                           Mode  Cnt   Score   Error   Units
QueueBench.oneThread               thrpt   45  89.300 ± 7.073  ops/us
QueueBench.oneThread:add1Thread    thrpt   45  41.292 ± 3.068  ops/us
QueueBench.oneThread:offersFailed  thrpt   45   0.905 ± 0.307  ops/us
QueueBench.oneThread:offersMade    thrpt   45  40.387 ± 3.114  ops/us
QueueBench.oneThread:pollsFailed   thrpt   45   7.621 ± 2.303  ops/us
QueueBench.oneThread:pollsMade     thrpt   45  40.387 ± 3.114  ops/us
QueueBench.oneThread:read1Thread   thrpt   45  48.008 ± 4.307  ops/us
* */


/*
* Merged tail_capacity and head_capacity into capacity
* Thought them being on diff cache lines might help, definitely did, thrpt reduced from 89 to 73 ops/s
* Benchmark                           Mode  Cnt   Score   Error   Units
QueueBench.oneThread               thrpt   45  73.590 ± 3.921  ops/us
QueueBench.oneThread:add1Thread    thrpt   45  33.007 ± 2.844  ops/us
QueueBench.oneThread:offersFailed  thrpt   45   2.306 ± 1.315  ops/us
QueueBench.oneThread:offersMade    thrpt   45  30.701 ± 2.086  ops/us
QueueBench.oneThread:pollsFailed   thrpt   45   9.882 ± 1.983  ops/us
QueueBench.oneThread:pollsMade     thrpt   45  30.701 ± 2.086  ops/us
QueueBench.oneThread:read1Thread   thrpt   45  40.583 ± 2.188  ops/us
*
* */


/* //Bit masking instead of if statements for wrap around,
* Benchmark                           Mode  Cnt   Score    Error   Units
QueueBench.oneThread               thrpt   45  84.869 ± 10.099  ops/us
QueueBench.oneThread:add1Thread    thrpt   45  40.362 ±  4.268  ops/us
QueueBench.oneThread:offersFailed  thrpt   45   1.256 ±  0.557  ops/us
QueueBench.oneThread:offersMade    thrpt   45  39.107 ±  4.730  ops/us
QueueBench.oneThread:pollsFailed   thrpt   45   5.400 ±  1.249  ops/us
QueueBench.oneThread:pollsMade     thrpt   45  39.107 ±  4.730  ops/us
QueueBench.oneThread:read1Thread   thrpt   45  44.507 ±  5.849  ops/us
*
* */

/* Seperated mask to both head and tail mask on different cache lines, variance and thrpt improved across most, so this seems much better
* Benchmark                           Mode  Cnt   Score   Error   Units
QueueBench.oneThread               thrpt   45  82.119 ± 6.096  ops/us
QueueBench.oneThread:add1Thread    thrpt   45  36.671 ± 3.522  ops/us
QueueBench.oneThread:offersFailed  thrpt   45   0.481 ± 0.165  ops/us
QueueBench.oneThread:offersMade    thrpt   45  36.191 ± 3.533  ops/us
QueueBench.oneThread:pollsFailed   thrpt   45   9.256 ± 2.028  ops/us
QueueBench.oneThread:pollsMade     thrpt   45  36.191 ± 3.533  ops/us
QueueBench.oneThread:read1Thread   thrpt   45  45.447 ± 2.866  ops/us //Though the variance is tighter, bit masking has worse perf than if branch wrap around?
*
* */





@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 10, time = 2)
@Measurement(iterations = 15, time = 2)
@Fork(3)
@State(Scope.Group)
public class QueueBench {

    Queue<Integer> queue;

    private final static int ITEM = 1;

    @Setup
    public void setup(){
        queue = new SPSCQueue<>(4096);
    }

    @AuxCounters
    @State(Scope.Thread)
    public static class PollCounters {
        public long pollsFailed;
        public long pollsMade;
    }

    @AuxCounters
    @State(Scope.Thread)
    public static class OfferCounters {
        public long offersFailed;
        public long offersMade;
    }

    @GroupThreads
    @Group("oneThread")
    @Benchmark
    public void add1Thread(Blackhole bh, OfferCounters counter){
        boolean off = queue.offer(ITEM);
        if (off) counter.offersMade++;
        else counter.offersFailed++;

        bh.consume(off);
    }

    @GroupThreads
    @Group("oneThread")
    @Benchmark
    public void read1Thread(Blackhole bh, PollCounters counter){
        Integer i = queue.poll();
        if (i == null) counter.pollsFailed++;
        else counter.pollsMade++;
        bh.consume(i);
    }


    public static class CounterRunner{
        void main() throws Exception {
            Options opt = new OptionsBuilder()
                    .include(QueueBench.class.getSimpleName())
                    .build();
            new Runner(opt).run();
        }
    }
}

