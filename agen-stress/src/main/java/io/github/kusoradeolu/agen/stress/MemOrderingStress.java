package io.github.kusoradeolu.agen.stress;

import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.II_Result;
import org.openjdk.jcstress.infra.results.I_Result;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

import static org.openjdk.jcstress.annotations.Expect.ACCEPTABLE;
import static org.openjdk.jcstress.annotations.Expect.ACCEPTABLE_INTERESTING;


//Oddly on x86 using an int as a flag doesn't give any stale results regardless of its memory ordering mode. Which is quite interesting
public class MemOrderingStress {
    @JCStressTest(Mode.Termination)
    @Outcome(id = "TERMINATED", expect = ACCEPTABLE,             desc = "Gracefully finished")
    @Outcome(id = "STALE",      expect = ACCEPTABLE_INTERESTING, desc = "Test is stuck")
    @State
    public static class PlainReadOpaqueWrite{
        volatile boolean ready;
        private static final VarHandle READY;

        static {
            try {
                READY = MethodHandles.lookup().findVarHandle(PlainReadOpaqueWrite.class, "ready", boolean.class);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }


        @Actor
        public void actor1() {
            while (!(boolean) READY.getOpaque(this)); // spin
        }

        @Signal
        public void signal() {
            READY.setOpaque(this, true);
        }
    }

}
