package io.github.kusoradeolu.agen.stress;

import io.github.kusoradeolu.agen.expr.queues.MPMCQueue;
import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.I_Result;


public class MPMCQueueStress {

    @JCStressTest
    @Outcome(id = "1",  expect = Expect.ACCEPTABLE, desc = "Reader got the element")
    @Outcome(id = "-1", expect = Expect.ACCEPTABLE, desc = "Reader ran before writer, element still in queue")
    @Outcome(id = "0",  expect = Expect.FORBIDDEN,  desc = "Element lost — neither reader got it nor queue has it")
    @State
    public static class AddRemoveTest {

        private final MPMCQueue queue = new MPMCQueue();

        @Actor
        public void writer() {
            queue.add(1);
        }

        @Actor
        public void reader(I_Result r) {
            r.r1 = queue.remove(1) ? 1 : -1;
        }

        @Arbiter
        public void arbiter(I_Result r) {
            if (r.r1 == -1 && !queue.contains(1)) {
                r.r1 = 0;
            }
        }
    }


    @JCStressTest
    @Outcome(id = "1", expect = Expect.ACCEPTABLE)
    @Outcome(id = "0", expect = Expect.FORBIDDEN, desc = "Chain broken")
    @State
    public static class AddRemoveChainTest {
        private final MPMCQueue queue = new MPMCQueue();
        { queue.add(1); queue.add(2); }

        @Actor public void remover() { queue.remove(1); }
        @Actor public void adder()   { queue.add(3); }

        @Arbiter public void arbiter(I_Result r) {
            r.r1 = (queue.contains(2) && queue.contains(3)) ? 1 : 0;
        }
    }

    @JCStressTest
    @Outcome(id = "1", expect = Expect.ACCEPTABLE)
    @Outcome(id = "0", expect = Expect.FORBIDDEN, desc = "Chain broken")
    @State
    public static class AddRemovePollChainTest {
        private final MPMCQueue queue = new MPMCQueue();
        {
            queue.add(1);
            queue.add(2);
        }

        @Actor public void remover() {
            queue.remove(1);
        }

        @Actor public void poller() {
            queue.poll();
        }

        @Actor public void adder()   {
            queue.add(3);
        }

        @Arbiter public void arbiter(I_Result r) {
            r.r1 = queue.canReachTail() ? 1 : 0;
        }
    }

}
