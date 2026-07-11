package io.github.kusoradeolu.agen.expr.counters;


import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLongArray;

/*
* Number of threads  = Number of total cores = number of threads incrementing this counter
* An atomic array with num of cores len is created
* Initially, when a thread first tries to append to the shared array using a set opaque flag, they increment a global atomic counter field to know which slot to work with
*
* Threads increment their own thread local field
* When their count exceeds > XX, when they get their global index and increment their value in the array, we use an integer array rather than an integer ref to prevent boxing
*
*  Sums are taken by iterating the whole array and using an opaque access to get the current count of each slot
* */
public class ThreadLocalCounter implements ApproximateCounter{
    private final ThreadLocal<long[]> localCounter;
    private final ThreadLocal<int[]> localIdx;


    private final int len;
    private final AtomicLongArray sharedCounter;
    private final AtomicInteger globalIdx;
    private static final int NO_IDX = -1;
    private static final int THRESHOLD = 1000;

    public ThreadLocalCounter() {
        //0 -> current count, 1 -> total count
        localCounter = ThreadLocal.withInitial(() -> new long[2]); //Lets try tracking our own local counter so we don't have to fetch the shared counter (which could cause a cache miss) and refresh the shared counter cache line on each sum
        localIdx = ThreadLocal.withInitial(() -> new int[]{NO_IDX});
        len = Runtime.getRuntime().availableProcessors();
        sharedCounter = fill(len);
        globalIdx = new AtomicInteger(0);
    }


    @Override
    public boolean add() {
        return this.add(1);
    }

    @Override
    public boolean add(long by) {
        if (by < 0) return false;
        var idxArr = localIdx.get();
        if (idxArr[0] == NO_IDX && (idxArr[0] = globalIdx.getAndIncrement()) >= len) return false;

        var cArr = localCounter.get();
        long curr = (cArr[0] += by);
        if (curr >= THRESHOLD) {
            int idx = idxArr[0];
            long newCount = (cArr[1] += curr);
            sharedCounter.setOpaque(idx, newCount);
            cArr[0] = 0; //Reset
        }

        return true;
    }

    @Override
    public long sum() {
        var sc = sharedCounter;
        long sum = 0;
        for (int i = 0; i < len; ++i){
            sum += sc.getOpaque(i);
        }

        return sum;
    }

    AtomicLongArray fill(int len){
        AtomicLongArray ref = new AtomicLongArray(len);
        for (int i = 0; i < len; ++i){
            ref.set(i, 0);
        }
        return ref;
    }
}
