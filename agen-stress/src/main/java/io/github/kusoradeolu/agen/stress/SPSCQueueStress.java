package io.github.kusoradeolu.agen.stress;

import io.github.kusoradeolu.agen.expr.queues.SPSCQueue;
import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.I_Result;

public class SPSCQueueStress {
    @JCStressTest
    @Outcome(id = "1",  expect = Expect.ACCEPTABLE, desc = "Reader got the element")
    @Outcome(id = "-1", expect = Expect.ACCEPTABLE, desc = "Reader ran before writer, element still in queue")
    @Outcome(id = "0",  expect = Expect.FORBIDDEN,  desc = "Element lost — neither reader got it nor queue has it")
    @State
    public static class AddRemoveTest {

        private final SPSCQueue<Integer> queue = new SPSCQueue<>(100);

        @Actor
        public void writer() {
            queue.add(1);
        }

        @Actor
        public void reader(I_Result r) {
            r.r1 = queue.poll() == null ? -1 : 1; //If we failed to read anything
        }

        @Arbiter
        public void arbiter(I_Result r) {
            //If the reader failed to read and remove anything and the queue does not contain the value, that means the reader actually removed the value but failed to read it, lost update
            if (r.r1 == -1 && !queue.contains(1)) {
                r.r1 = 0;
            }
        }
    }
}
