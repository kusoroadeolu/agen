package io.github.kusoradeolu.agen.stress;

import io.github.kusoroadeolu.mem.coherence.*;
import io.github.kusoroadeolu.mem.coherence.LockedObject.EpochBundle;
import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.II_Result;
import org.openjdk.jcstress.infra.results.I_Result;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Random;

//Cache coherence invariant stress
//We want to stress the SWMR Invariant and the data value invariant
public class CCInvariantStress {

    //For the single writer multiple reader invariant, we want to lock the map, get the map provided the MP chip, then we traverse the map, if any RW epoch has a writer > 1, we count that as invalid, otherwise, we count it as valid
    @JCStressTest
    @Outcome(id = "1", expect = Expect.ACCEPTABLE, desc = "SW invariant maintained")
    @Outcome(id = "-1", expect = Expect.ACCEPTABLE_INTERESTING, desc = "Failed to hold write lock")
    @Outcome(id = "0", expect = Expect.FORBIDDEN, desc = "SW invariant violated")
    @State
    public static class SWMRInvariant{
        private final MemoryLocation memoryLocation;

        public SWMRInvariant() {
            this.memoryLocation = new MemoryLocation();
        }

        @Actor
        public void writer(){
            MultiProcessorChip.chip().write(memoryLocation, System.currentTimeMillis());
        }

        @Actor
        public void reader(){
            MultiProcessorChip.chip().read(memoryLocation);
        }

        @Arbiter
        //Checks state
        public void arbiter(I_Result res){
            var lo = MultiProcessorChip.chip().mainMemory().get(memoryLocation);
            if (lo.tryHoldWrite()){
                try {
                    Map<Integer, EpochBundle> map = lo.epochMap();
                    for (Map.Entry<Integer, EpochBundle> entry : map.entrySet()){
                        var value = entry.getValue();
                        if (value.epoch() == Epoch.RW_EPOCH && value.count().get() > 1){
                            res.r1 = 0;
                            return;
                        }
                    }
                    res.r1 = 1;
                    return;

                }finally {
                    lo.releaseWrite();
                }
            }


            res.r1 = -1;
        }

    }



    @JCStressTest
    @Outcome(id = "1", expect = Expect.ACCEPTABLE, desc = "DV invariant maintained")
    @Outcome(id = "0", expect = Expect.FORBIDDEN, desc = "DV invariant violated")
    @State
    public static class DataValueInvariant{
        private final MemoryLocation memoryLocation;


        public DataValueInvariant() {
            this.memoryLocation = new MemoryLocation();
        }

        @Actor
        public void writer(){
            MultiProcessorChip.chip().write(memoryLocation, System.currentTimeMillis());
        }

        @Actor
        public void reader(){
            MultiProcessorChip.chip().read(memoryLocation);
        }

        @Arbiter
        //Checks state
        public void arbiter(I_Result res){
            var lo = MultiProcessorChip.chip().mainMemory().get(memoryLocation);
                try {
                    lo.holdWrite();
                    Long lastRWValue = -1L;
                    Map<Integer, EpochBundle> map = lo.epochMap();
                    for (Map.Entry<Integer, EpochBundle> entry : map.entrySet()){
                        var value = entry.getValue();
                        if (value.epoch() == Epoch.RW_EPOCH){
                            lastRWValue = (Long) value.value();
                            return;
                        }else if (value.epoch() == Epoch.RO_EPOCH) {
                            if (lastRWValue != value.value()) res.r1 = 0;
                        }
                    }
                    res.r1 = 1;
                }finally {
                    lo.releaseWrite();
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
}
