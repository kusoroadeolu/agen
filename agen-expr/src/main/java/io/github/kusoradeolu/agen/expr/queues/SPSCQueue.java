package io.github.kusoradeolu.agen.expr.queues;


import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.Queue;

//A bounded SPSC queue built fully from intuition
/*
 * Two fields, head -> tail
 * Head where a consumer reads from
 * Tail where a producer writes to
 *
 * Invariants. The head idx can never be greater than the tail idx
 * Only one producer/consumer can write to this data structure
 * A write to an idx must be visible before the head(if its at that idx) is advanced
 * An added item can never be null
 *
 * Algorithm:
 *   Add:
 *     We read the size
 *     if the size < cap - 1, then the array is full, or items[tail]  != null, our slot has not yet been read, we return
 *     else we write to the current tail slot
 *     to ensure the write is visible, we use a volatile barrier (for now)
 *     we then advance our tail, if our tail == capacity, we reset to zero
 *
 *
 *   Remove:
 *     if our items[head] == null, we return null
 *     else we null out items[head]
 *     to ensure the write is visible, we use a volatile barrier (for now)
 *     we then advance our head, if our head == capacity, we reset to zero
 *
 * */

class SPSCConsumerPad {
    int cIdx;
    int c_mask;
    byte b000,b001,b002,b003,b004,b005,b006,b007;//  8b
    byte b010,b011,b012,b013,b014,b015,b016,b017;// 16b
    byte b020,b021,b022,b023,b024,b025,b026,b027;// 24b
    byte b030,b031,b032,b033,b034,b035,b036,b037;// 32b
    byte b040,b041,b042,b043,b044,b045,b046,b047;// 40b
    byte b050,b051,b052,b053,b054,b055,b056,b057;// 48b
    byte b060;

    public SPSCConsumerPad(int mask){
        c_mask = mask;
    }

}


class SPSCProducerPad extends SPSCConsumerPad {
    int pIdx; //Head and tail are susceptible to false sharing
    int p_mask;
    byte b000,b001,b002,b003,b004,b005,b006,b007;//  8b
    byte b010,b011,b012,b013,b014,b015,b016,b017;// 16b
    byte b020,b021,b022,b023,b024,b025,b026,b027;// 24b
    byte b030,b031,b032,b033,b034,b035,b036,b037;// 32b
    byte b040,b041,b042,b043,b044,b045,b046,b047;// 40b
    byte b050,b051,b052,b053,b054,b055,b056,b057;// 48b
    byte b060;

    public SPSCProducerPad(int mask) {
        super(mask);
        p_mask = mask;
    }
}

public class SPSCQueue<T> extends SPSCProducerPad implements Queue<T> {
    private final T[] items;
    private final int capacity;

    private static final VarHandle ITEMS;

    static {
        ITEMS = MethodHandles.arrayElementVarHandle(Object[].class);
    }

    @SuppressWarnings("unchecked")
    public SPSCQueue(int capacity) {
        int toPowTwo = 1 << (32 - Integer.numberOfLeadingZeros(capacity - 1));
        this.capacity = toPowTwo;
        super(toPowTwo - 1);
        this.items = (T[]) new Object[toPowTwo];
    }

    public boolean add(T item){
        Objects.requireNonNull(item);
        if (ITEMS.getAcquire(items, pIdx) != null) return false;
        ITEMS.setRelease(items, pIdx, item); //A weaker set opaque ordering would be alright here
        maskTail();
        return true;
    }

    @SuppressWarnings("unchecked")
    public T poll(){
        T o = (T) ITEMS.getAcquire(items, cIdx);
        if (o == null) return null;
        ITEMS.setRelease(items, cIdx, null);
        maskHead();
        return o;
    }

    public boolean contains(Object val){
        Objects.requireNonNull(val);
        for (T item : items){
            if (val.equals(item)) return true;
        }

        return false;
    }

    @Override
    public boolean offer(T t) {
        return add(t);
    }

    @Override
    public int size() {
        int size = 0;
        for (int i = 0; i < capacity; ++i){
            if (ITEMS.getAcquire(items, i) != null) ++size;
        }

        return size;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public Iterator<T> iterator() {
        return null;
    }

    @Override
    public Object[] toArray() {
        return new Object[0];
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        return null;
    }

    @Override
    public boolean remove(Object o) {
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return false;
    }

    @Override
    public void clear() {

    }

    @Override
    public T remove() {
        return null;
    }

    @Override
    public T element() {
        return null;
    }

    @Override
    public T peek() {
        return null;
    }


    public void maskHead(){
        cIdx = (cIdx + 1) & c_mask;
    }

    public void maskTail(){
        pIdx = (pIdx + 1) & p_mask;
    }
}
