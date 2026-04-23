package io.github.kusoradeolu.agen.expr.counters;


import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;

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
    private final AtomicReferenceArray<Long> sharedCounter;
    private final AtomicInteger globalIdx;
    private static final int NO_IDX = -1;
    private static final int THRESHOLD = 50;

    public ThreadLocalCounter() {
        localCounter = ThreadLocal.withInitial(() -> new long[1]);
        localIdx = ThreadLocal.withInitial(() -> new int[]{NO_IDX}); //Sta
        len = Runtime.getRuntime().availableProcessors();
        sharedCounter = prefill(len);
        globalIdx = new AtomicInteger(0);
    }


    @Override
    public boolean add() {
        return this.add(1);
    }

    @Override
    public boolean add(long by) {
        if (by < 0) return false;
        var sc = sharedCounter;
        var idxArr = localIdx.get();
        if (idxArr[0] == NO_IDX && (idxArr[0] = globalIdx.getAndIncrement()) >= len) return false;

        var cArr = localCounter.get();
        long curr = (cArr[0] += by);
        int idx = idxArr[0];
        if (curr >= THRESHOLD) {
            long prev = sc.getOpaque(idx);
            sc.setOpaque(idx, prev + curr);
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

    AtomicReferenceArray<Long> prefill(int len){
        AtomicReferenceArray<Long> ref = new AtomicReferenceArray<>(len);
        for (int i = 0; i < len; ++i){
            ref.set(i, 0L);
        }

        return ref;
    }
}
