package io.github.kusoradeolu.agen.stress;

import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.II_Result;

//The JMM is said to use a data free mem consistency model that brings the simplicity of seq consistency while allowing compiler optimizations
// To test this, ill be using two volatile variables to simulate sequential consistency and validate if this guarantee holds
@State
@JCStressTest
@Outcome(id = "1, 1", expect = Expect.FORBIDDEN, desc = "seq consistency guarantee violated")
@Outcome(id = {"1, 0", "0, 1", "0, 0"}, expect = Expect.ACCEPTABLE, desc = "seq consistency guarantee maintained")
public class SeqConsistencyStress {
    public volatile boolean done;
    public volatile int a;

    /*
     * Thread 1
     * L1: done
     * S1: a
     *
     *
     * Thread 2
     * L2: a
     * S2: done
     * */

    /*
    * Invalid reorderings
    * S1 -> L1 -> S2 -> L2 a = 1, b = true
    * S1 -> S2 -> L1 -> L2 a = 1, b = true
    * S2 -> L2 -> S1 -> L1 a = 1, b = true
    * S2 -> S1 -> L2 -> L1 a = 1, b = true
    * */
    @Actor
    public void setActor(){
        boolean b = done;
        a = 1; //Store/Load fences to ensure seq consistency done -> a, cannot be reordered after the other

    }

    @Actor
    public void doneActor(){
        int b = a;
        done = true; //
    }

    @Arbiter
    public void arbiter(II_Result res){
        res.r1 = a == 1 ? 0 : 1;
        res.r2 = done ? 0 : 1;
    }

}
