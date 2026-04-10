package io.github.kusoradeolu.agen.expr.counters;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DiminishedSynchronizedCounter implements ApproximateCounter{
    private final Lock lock;
    private final AtomicLong sum;
    private final Queue<Long> buffer; // Blocking for backpressure
    private static final int MAX_ADD = 100; //At most dont process more than 100 values at a time to prevent a livelock

    public DiminishedSynchronizedCounter() {
        this.lock = new ReentrantLock();
        this.sum = new AtomicLong(0);
        this.buffer = new ConcurrentLinkedQueue<>();
    }

    @Override
    public boolean add() {
        return add(1);
    }

    @Override
    public boolean add(long by) {
        if (by < 1) return false;
        if (lock.tryLock()){
            try {
                sum.addAndGet(by);
                Long val;
                int count = 0;
                while ((val = buffer.poll()) != null && ++count != MAX_ADD){
                    sum.addAndGet(val);
                }
                return true;
            }finally {
                lock.unlock();
            }
        }

        buffer.add(by);
        return false;
    }

    @Override
    public long sum() {
        return sum.get();
    }
}
