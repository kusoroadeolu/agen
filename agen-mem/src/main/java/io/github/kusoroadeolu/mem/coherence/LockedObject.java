package io.github.kusoroadeolu.mem.coherence;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class LockedObject{
        private final ReentrantReadWriteLock status;
        private volatile Object object;
        private volatile Epoch epoch;
        private int currentEpoch;
        private final Map<Integer, EpochBundle> epochMap; //Epoch to object, just to keep track of our invariants

        public LockedObject() {
            this.status = new ReentrantReadWriteLock();
            this.epochMap = new HashMap<>();
        }

       public boolean tryHoldWrite(){
           //SWMR Invariant is held by the serializability guarantees, that only on thread can hold this write lock at any time, of the write lock
           boolean held = status.writeLock().tryLock();
           if (held) {
               epoch = Epoch.RW_EPOCH;
               var bundle = new EpochBundle(Epoch.RW_EPOCH, new AtomicInteger(0));
               epochMap.put(++currentEpoch, bundle);
           }
           return held;
        }

        boolean tryHoldRead(){
            boolean held = status.readLock().tryLock();

            if (held) {
                if (epoch != Epoch.RO_EPOCH) ++currentEpoch; //If we were not already in a read epoch
                epoch = Epoch.RO_EPOCH;
                epochMap.putIfAbsent(currentEpoch, new EpochBundle(epoch, object, new AtomicInteger(0))); //Doesn't matter how many times we put this honestly
            }

            return held;
        }

        public void holdWrite(){
            status.writeLock().lock();
        }

       public void releaseWrite(){
            status.writeLock().unlock();
        }

        void releaseRead(){
            status.readLock().unlock();
        }

        //Should only be written to if READ_WRITE is held
        void setValue(Object value){
            this.object = value;
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

        EpochBundle(Epoch epoch, Object o ,AtomicInteger count) {
            this.epoch = epoch;
            this.count = count;
            this.value = o;
        }

        public void setValue(Object value) {
            this.value = value;
        }

        public Epoch epoch() { return epoch; }
        public Object value() { return value; }
        public AtomicInteger count() { return count; }
    }    }