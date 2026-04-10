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

        public void toRWEpoch(){
            epoch = Epoch.RW_EPOCH;
        }

        public void toROEpoch(){
            epoch = Epoch.RO_EPOCH;
        }

        public Epoch epoch(){
            return epoch;
        }

        //Should only be written to if READ_WRITE is held
        void setValue(Object value){
            this.object = value;
        }

        public Object getCurrentValue(){
            return this.object;
        }
    }
}
