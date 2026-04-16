package io.github.kusoradeolu.agen.expr.fc;


import java.lang.invoke.VarHandle;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/*
* A flat combiner that works based on the idea of a SPSC Queue
* A flat combiner simply said is a synchronization primitive that works on the idea that batching requests while a thread is holding a lock proves to be more performant than the cost of acquiring one under high concurrency
*
* To start, we have a sequencer object which wraps a simple atomic long, and will produce a sequence or a ticket for a thread to own to guarantee fairness
*
* We categorize threads into two categories:
* Combiner - The thread who holds the lock and will perform requests for other threads and also increments the `ticketSequencer` number
* Non combiners - Threads who submit their requests to the combining threads and spin on their sequence. Also increments the `pubSequence`
*
* We have 5 fields in this class
*
* An atomic field (for visibility guarantees when a request is written to the field), stores the next request for a combiner to process after it has removed a previous request
* A ticket sequencer which non-combiners draw from to form an order in which non combiners will write in the shared field when the combiner has drawn from the field
* A sequencer for the combiner to indicate the next non combiner to put their request in request field
* An FCLock representing a simple try lock
* The shared structure/field we're acting on
*
* No queue is explicitly created but the use of sequencers as concurrency control creates an abstract queue that allows fairness while ensuring correctness and preventing extra object creation allocation of node objects unlike other flat combiners
* To prevent the number of contended hotspots in this combiner, we use a simple atomic long as the ticket sequencer (accessed by multiple threads only once to get their ticket number) and an atomic long sequencer as the `pubSequence` number
* To lessen the number of cas retries on the atomic boolean try lock, non combiners spin briefly on the "pubSequence" field, checking if they can input their request in the shared field before they write to it
*
*
*
* Invariants:
* A thread must not submit a null request
* Both sequencers must be monotonically increasing
* The combining thread must always null the shared request field before incrementing the pubSequence
* A combining thread cannot combine more than x requests at a time
* A non combiner cannot acquire the lock if its sequence number does not equal the current pub sequence number
* A non combiner cannot write to the share request field if its sequence number does not equal the current pub sequence number
*
*
* Proposed algorithm, might not be final
* on entry
*   no = ticketSeq.next  //2
*
*   repeatedly
*     if pubSequence.curr == no && cas(fclock, unacq, acq){
*         repeatedly
*           if(req is applied){
*               req.apply()
*               sharedPubField = null; //Plain write here
*               pubSequence.increment(); //Backed by a volatile write
*           }
*
*           combineCount = 1
*           repeatedly if(sharedPubField != null && combineCount != maxCombineCount){
*              x = sharedPubField
*              sharedPubField = null; //Plain write
*              pubSequence.increment() //Backed by a volatile write
*              x.apply()
*              increment combineCount
*           }
*     }
*
*
*    repeatedly up to x
*      if pubSequence != no {
*         //Keep checking
*
*      }else {
*        //Write to the shared field and spin on it to be applied //Volatile write here
*        if there isn't a combiner and we're to become the next combiner, restart from outer
*       }
*
* */
public class SequencerFlatCombiner<E> {

    static class Action<E>{
        final Consumer<E> runnable;
        final AtomicBoolean applied;

        public Action(Consumer<E> runnable) {
            this.runnable = runnable;
            this.applied = new AtomicBoolean();
        }

        void apply(E e){
            runnable.accept(e);
            applied.set(true);
        }

        boolean isApplied() {
            return applied.get();
        }
    }

    static class Sequencer {
        final AtomicLong seq;

        public Sequencer() {
            this.seq = new AtomicLong();
        }

        public long nextSequence(){
            return seq.getAndIncrement();
        }

        public long currentSeq(){
            return seq.get();
        }
    }

    static class FCLock{
        final AtomicBoolean lock;

        public FCLock() {
            this.lock = new AtomicBoolean(false);
        }

        public boolean tryLock(){
            return lock.compareAndSet(false, true);
        }

        public void release(){
            lock.compareAndSet(true, false);
        }

        public boolean isHeld(){
            return lock.get();
        }
    }

    final AtomicReference<Action<E>> pubAction;
    long l1, l2, l3, l4, l5, l6, l7, l8, l9, l10, l11;
    final Sequencer pubSeq;
    long l12, l13, l14, l15, l16, l17, l18, l19, l20, l21;
    final Sequencer ticketSeq;
    long l22, l23, l24, l25, l26, l27, l28, l29, l30, l31;
    final FCLock lock;
    final E e; //Shared field

    public SequencerFlatCombiner(E e) {
        Objects.requireNonNull(e);
        this.pubAction = new AtomicReference<>();
        this.pubSeq = new Sequencer();
        this.ticketSeq = new Sequencer();
        this.lock = new FCLock();
        this.e = e;
    }


    public Void combine(Consumer<E> consumer){
        var action = new Action<>(consumer);
        long ticket = ticketSeq.nextSequence();
        int maxSpinCount = 100;

        outer:  while (!action.isApplied()){
            //If our current ticket == ticketSeq.nextSeq, meaning we're next in line for our consumer to be applied
            if (lock.tryLock()){
                if(!action.isApplied()){ //If our action has not yet been applied
                   action.apply(e);
                   pubAction.setPlain(null);
                   pubSeq.nextSequence(); //Volatile write will make "null write" visible
                   if (pubSeq.currentSeq() == ticket) pubSeq.nextSequence();
                }

                int combineCount = 0;
                Action<E> local;
                while ((local = pubAction.get()) != null && combineCount != 100){
                    pubAction.setPlain(null);
                    pubSeq.nextSequence(); //Volatile write will make "null write" visible
                    if (pubSeq.currentSeq() == ticket) pubSeq.nextSequence();
                    local.apply(e);
                    combineCount++;
                }
                lock.release();
                return null;
            }

            int count = 0;
            while (count++ < maxSpinCount && !action.isApplied()){
                if (pubSeq.currentSeq() == ticket){
                    pubAction.set(action);
                    if (!lock.isHeld()) continue outer;
                }

            }
       }

        return null;
    }

    //Yeah this genuinely failed haha, the batching using an "abstract queue" basically will not work in practice because threads are non-deterministic.
    //My current implementation requires, that threads deterministically always write to the shared buffer immediately their sequence number becomes visible, but this might not always be the case because the combining thread might have finished with a previous request before a thread even writes to a shared field

}
