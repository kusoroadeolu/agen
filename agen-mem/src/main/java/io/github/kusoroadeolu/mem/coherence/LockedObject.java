package io.github.kusoroadeolu.mem.coherence;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class LockedObject{
        private final ReadWriteLock status;
        private volatile Object object;
        private volatile Epoch epoch;
        private int currentEpoch;
        private final Map<Integer, EpochBundle> epochMap; //Epoch to object, just to keep track of our invariants

        public LockedObject() {
            this.status = new ReentrantReadWriteLock();
            this.epochMap = new HashMap<>();
        }

        boolean tryHoldWrite(){
           boolean held = status.writeLock().tryLock();
           if (held) {
               epoch = Epoch.RW_EPOCH;
               ++currentEpoch;
           }

           return held;
        }

        boolean tryHoldRead(){
            boolean held = status.readLock().tryLock();
            if (epoch != Epoch.RO_EPOCH) ++currentEpoch; //If we were not already in a read epoch

            if (held) {
                epoch = Epoch.RO_EPOCH;
                epochMap.putIfAbsent(currentEpoch, new EpochBundle(epoch, object)); //Doesn't matter how many times we put this honestly
            }

            return held;
        }

        void releaseWrite(){
            status.writeLock().unlock();
        }

        void releaseRead(){
            status.readLock().unlock();
        }

        //Should only be written to if READ_WRITE is held
        void setValue(Object value){
            this.epochMap.put(currentEpoch, new EpochBundle(epoch, value));
            this.object = value;
        }

        Object getValue(){
           return this.object;
        }

        record EpochBundle(Epoch epoch, Object value){}
    }