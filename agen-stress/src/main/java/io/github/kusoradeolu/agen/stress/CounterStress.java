package io.github.kusoradeolu.agen.stress;


import io.github.kusoradeolu.agen.expr.counters.ApproximateCounter;
import io.github.kusoradeolu.agen.expr.counters.SynchronizedCounter;
import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.I_Result;

import java.util.concurrent.ThreadLocalRandom;

public class CounterStress {

    @JCStressTest
    @State
    @Outcome(id = "0", expect = Expect.ACCEPTABLE_INTERESTING, desc = "Monotonic invariant violated")
    @Outcome(id = "1", expect = Expect.ACCEPTABLE, desc = "Monotonic invariant maintained")

    public static class CounterMonoticSumInvariant {
        private final ApproximateCounter counter = new SynchronizedCounter();

        @Actor
        public void add(){
            counter.add(ThreadLocalRandom.current().nextLong());
        }

        @Arbiter
        public void arbiter(I_Result res){
            long sum = counter.sum();
            if (sum < 0) res.r1 = 0;
            else res.r1 = 1;
        }
    }


    @JCStressTest
    @State
    @Outcome(id = "0", expect = Expect.ACCEPTABLE_INTERESTING, desc = "Monotonic invariant violated")
    @Outcome(id = "1", expect = Expect.ACCEPTABLE, desc = "Monotonic invariant maintained")

    public static class CounterIncrementInvariant {
        private final ApproximateCounter counter = new SynchronizedCounter();

        @Actor
        public void add(){
            counter.add(0); //Always add 0, sum must always remain 0
        }

        @Arbiter
        public void arbiter(I_Result res){
            long sum = counter.sum();
            if (sum > 0) res.r1 = 0;
            else res.r1 = 1;
        }
    }
}
