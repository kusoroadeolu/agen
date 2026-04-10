package io.github.kusoradeolu.agen.expr.counters;

import java.util.concurrent.atomic.AtomicLong;

public class SynchronizedCounter implements ApproximateCounter{
    private final Object lock;
    private final AtomicLong sum;

    public SynchronizedCounter() {
        this.lock = new Object();
        this.sum = new AtomicLong(0);
    }

    @Override
    public boolean add() {
       return add(1);
    }

    @Override
    public boolean add(long by) {
        if (by < 1) return false;
        synchronized (lock){
            sum.addAndGet(by);
        }

        return true;
    }

    @Override
    public long sum() {
        return sum.get();
    }
}
