package io.github.kusoradeolu.agen.expr.counters;

/**
 * 3 monotonically increasing counters, whose `sum()` reads are never guaranteed to be consistent at read time. A simple synchronized counter with a big lock
 * An simple synchronized approx counter, if the lock is held, `value` to be incremented is put into a write buf. Before the `holder` of the lock releases the lock, we drain the write buffer
 * A segmented simple synchronized approx counter. The sum is calculated by reading and adding the sum of all segments
 *
 *
 * Invariants. 3 simple invariants
 * `sum()` reads must be lock free across all implementations of this class. Each implementation of this class is not required to keep a cache a sum field, however in the case a sum field the field must be marked as volatile, to ensure atomic reads of(long/double) values and immediate visibility of writes
 *  In the case a shared `sum` field is written to, the field must be properly synchronized to prevent data races.
 *  Increments to a shared field must be greater than zero
 *
 *  This is an experiment that mainly tests how the presence of a write buffer diminishes the overhead of locking(similar to what flat combining does, but threads don't wait on a result)
 * */
public interface ApproximateCounter {
    boolean add();
    boolean add(long by);
    long sum();
}
