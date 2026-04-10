package io.github.kusoradeolu.agen.stress;

import io.github.kusoroadeolu.mem.coherence.MainMemory;
import io.github.kusoroadeolu.mem.coherence.MemoryLocation;
import io.github.kusoroadeolu.mem.coherence.MultiProcessorChip;
import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.I_Result;

//Cache coherence invariant stress
public class CCInvariantStress {


    @JCStressTest
    @Outcome(id = "1", expect = Expect.ACCEPTABLE, desc = "DV invariant maintained")
    @Outcome(id = "-1", expect = Expect.ACCEPTABLE_INTERESTING, desc = "Failed to start ack")
    @Outcome(id = "0", expect = Expect.FORBIDDEN, desc = "DV invariant violated")
    @State
    public static class DataValueInvariant {
        private final MemoryLocation memoryLocation;
        private final MainMemory memory;
        private volatile long lastRWObject = -1;


        public DataValueInvariant() {
            this.memoryLocation = new MemoryLocation();
            this.memory = new MainMemory();
        }

        //Basically how the cache protocol works internally
        @Actor
        public void actor(I_Result result) {
                if(memory.startAck()){
                    if (lastRWObject != -1 &&
                            lastRWObject != (long) memory.get(memoryLocation).getCurrentValue()) {
                        result.r1 = 0;
                    }else {
                        result.r1 = 1;
                        long time = System.currentTimeMillis();
                        memory.write(memoryLocation, time);
                        lastRWObject = time;
                    }
                    memory.endAck();

                }else {
                    result.r1 = -1;
                }
        }


        @Actor
        public void actor1(I_Result result) {
            if(memory.startAck()){
                if (lastRWObject != -1 &&
                        lastRWObject != (long) memory.get(memoryLocation).getCurrentValue()) {
                    result.r1 = 0;
                }else {
                    result.r1 = 1;
                    long time = System.currentTimeMillis();
                    memory.write(memoryLocation, time);
                    lastRWObject = time;
                }
                memory.endAck();

            }else {
                result.r1 = -1;
            }
        }
    }

        @JCStressTest
        @Outcome(id = "1", expect = Expect.ACCEPTABLE, desc = "invariant maintained")
        @Outcome(id = "0", expect = Expect.FORBIDDEN, desc = "invariant violated")
        @State
        public static class FinalFieldVisibility{


            @Actor
            public void writer(I_Result result){
                result.r1 = new Field().i; //Should always be 1
            }

            static class Field{
                final int i;

                public Field() {
                    this.i = 1;
                }
            }
        }
    }
