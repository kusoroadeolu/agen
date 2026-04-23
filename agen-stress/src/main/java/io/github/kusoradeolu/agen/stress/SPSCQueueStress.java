package io.github.kusoradeolu.agen.stress;

import io.github.kusoradeolu.agen.expr.queues.SPSCQueue;
import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.I_Result;

public class SPSCQueueStress {
    @JCStressTest
    @Outcome(id = "1",  expect = Expect.ACCEPTABLE, desc = "Reader got the element")
    @State
    public static class ConsumerTest {

        private final SPSCQueue<Integer> queue = new SPSCQueue<>(3);

        public ConsumerTest() {
            queue.offer(1); queue.offer(2); queue.offer(3);
        }

        @Actor
        public void actor() {
            queue.poll(); queue.poll();
        }


        @Arbiter
        public void arbiter(I_Result r) {
            r.r1 = queue.size();
        }
    }
}
