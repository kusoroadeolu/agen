package io.github.kusoradeolu.agen.stress;

import io.github.kusoroadeolu.mem.coherence.Epoch;
import io.github.kusoroadeolu.mem.coherence.MainMemory;
import io.github.kusoroadeolu.mem.coherence.MemoryLocation;
import io.github.kusoroadeolu.mem.coherence.MultiProcessorChip;
import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.I_Result;

import java.util.Map;

//Cache coherence invariant stress
//We want to stress the SWMR Invariant and the data value invariant
public class CCInvariantStress {

    //For the single writer multiple reader invariant, we want to lock the map, get the map provided the MP chip, then we traverse the map, if any RW epoch has a writer > 1, we count that as invalid, otherwise, we count it as valid
    @JCStressTest
    @Outcome(id = "1", expect = Expect.ACCEPTABLE, desc = "SW invariant maintained")
    @Outcome(id = "-1", expect = Expect.ACCEPTABLE_INTERESTING, desc = "Failed to start ack")
    @Outcome(id = "0", expect = Expect.FORBIDDEN, desc = "SW invariant violated")
    @State
    public static class SWMRInvariant{
        private final MemoryLocation memoryLocation;

        public SWMRInvariant() {
            this.memoryLocation = new MemoryLocation();
        }

        @Actor
        public void writer(){
            MultiProcessorChip.chip().store(memoryLocation, System.currentTimeMillis());
        }

        @Actor
        public void reader(){
            MultiProcessorChip.chip().load(memoryLocation);
        }

        @Arbiter
        //Checks state
        public void arbiter(I_Result res){
            var lo = MultiProcessorChip.chip().memory().get(memoryLocation);
            if (MultiProcessorChip.chip().memory().startAck()){
                try {
                    Map<Integer, MainMemory.EpochObject.EpochBundle> map = lo.epochMap();
                    for (Map.Entry<Integer, MainMemory.EpochObject.EpochBundle> entry : map.entrySet()){
                        var value = entry.getValue();
                        if (value.epoch() == Epoch.RW_EPOCH && value.count().get() > 1){
                            res.r1 = 0;
                            return;
                        }
                    }
                    res.r1 = 1;
                    return;

                }finally {
                    MultiProcessorChip.chip().memory().endAck();
                }
            }


            res.r1 = -1;
        }

    }



    @JCStressTest
    @Outcome(id = "1", expect = Expect.ACCEPTABLE, desc = "DV invariant maintained")
    @Outcome(id = "0", expect = Expect.FORBIDDEN, desc = "DV invariant violated")
    @State
    public static class DataValueInvariant {
        private final MemoryLocation memoryLocation;


        public DataValueInvariant() {
            this.memoryLocation = new MemoryLocation();
        }

        @Actor
        public void writer() {
            MultiProcessorChip.chip().store(memoryLocation, System.currentTimeMillis());
        }

        @Actor
        public void reader() {
            MultiProcessorChip.chip().load(memoryLocation);
        }

        @Arbiter
        //Checks state
        public void arbiter(I_Result res) {
            var lo = MultiProcessorChip.chip().memory().get(memoryLocation);
            if (MultiProcessorChip.chip().memory().startAck()) {
                try {
                    Long lastRWValue = -1L;
                    Map<Integer, MainMemory.EpochObject.EpochBundle> map = lo.epochMap();
                    for (Map.Entry<Integer, MainMemory.EpochObject.EpochBundle> entry : map.entrySet()) {
                        var value = entry.getValue();
                        if (value.epoch() == Epoch.RW_EPOCH) {
                            lastRWValue = (Long) value.value();
                        } else if (value.epoch() == Epoch.RO_EPOCH) {
                            if (lastRWValue != value.value()) {
                                res.r1 = 0;
                                return;
                            }
                        }
                    }
                    res.r1 = 1;
                } finally {
                    MultiProcessorChip.chip().memory().endAck();
                }
            }
        }
    }
}
