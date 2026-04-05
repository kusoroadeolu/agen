package io.github.kusoroadeolu.mem.coherence;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

//To simplify things, we treat the main memory as sequential when a cache is requesting for an ack, if we fail to start an ack, we just return
public class MainMemory{

    private final AtomicBoolean ack;
    private final Map<MemoryLocation, EpochObject> map;

    public MainMemory() {
        this.ack = new AtomicBoolean(false);
        this.map = new ConcurrentHashMap<>();
    }


    //Should only be called in tests or once we've started an ack
    public EpochObject get(MemoryLocation location){
        return map.computeIfAbsent(location ,_ -> new EpochObject());
    }

    public boolean startAck(){
        return ack.compareAndSet(false, true);
    }

    public void endAck(){
        ack.lazySet(false);
    }

    public void write(MemoryLocation location, Object o){
        var lo = map.computeIfAbsent(location ,_ -> new EpochObject());
        lo.toRWEpoch();
        lo.setValue(o);
    }

    public Object read(MemoryLocation location){
        var lo = map.computeIfAbsent(location ,_ -> new EpochObject());
        lo.toROEpoch();
        return lo.getCurrentValue();
    }


    public static class EpochObject {
        private volatile Object object;
        private volatile Epoch epoch;
        private int currentEpoch;
        private final Map<Integer, EpochBundle> epochMap; //Epoch to object, just to keep track of our invariants

        public EpochObject() {
            this.epochMap = new HashMap<>();
        }

        public void toRWEpoch(){
            //SWMR Invariant is held by the serializability guarantees, that only on thread can hold this write lock at any time, of the write lock
            epoch = Epoch.RW_EPOCH;
            var bundle = new EpochBundle(Epoch.RW_EPOCH, new AtomicInteger(1));
            epochMap.put(++currentEpoch, bundle);
        }

        public void toROEpoch(){
            if (epoch == Epoch.RO_EPOCH) return;
            //SWMR Invariant is held by the serializability guarantees, that only on thread can hold this write lock at any time, of the write lock
            epoch = Epoch.RO_EPOCH;
            var bundle = new EpochBundle(Epoch.RW_EPOCH, new AtomicInteger(0));
            epochMap.put(++currentEpoch, bundle);
        }


        //Should only be written to if READ_WRITE is held
        void setValue(Object value){
            this.object = value;
            epochMap.get(currentEpoch).setValue(value);
        }

        public Object getCurrentValue(){
            if (epoch == Epoch.RO_EPOCH) epochMap.get(currentEpoch).count.incrementAndGet();
            return this.object;
        }

        public Map<Integer, EpochBundle> epochMap(){
            return this.epochMap;
        }

        public static class EpochBundle {
            private final Epoch epoch;
            private Object value;
            private final AtomicInteger count;

            EpochBundle(Epoch epoch, AtomicInteger count) {
                this.epoch = epoch;
                this.count = count;
            }

            public void setValue(Object value) {
                this.value = value;
            }

            public Epoch epoch() { return epoch; }
            public Object value() { return value; }
            public AtomicInteger count() { return count; }
        }
    }
}
