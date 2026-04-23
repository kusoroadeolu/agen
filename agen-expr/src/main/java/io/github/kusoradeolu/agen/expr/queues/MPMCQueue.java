package io.github.kusoradeolu.agen.expr.queues;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;


/*
* A queue with no tail field to promote simplicity
*
* Invariants: Only the tail's next node must be equal to "null"
* Linearizability point of a node to be removed -> value set to null
* Linearizability point of a node to be enqueued -> seen tail "next" successfully cas'd to the node
* The tail must always be reachable from the head no matter the interleaving
*
*
* CURRENT CONSTRAINTS:
* Dead nodes may persist since dead nodes are eagerly pruned. Under high concurrency, if a node's pred node is dead, it might not be removed.
* To potentially fix this if a pred node is dead, we could try walking from the head to remove the node
* *
* Two invariants 1. No lost updates and 2. Reachability from head updates have been tested with JCStress
* Built fully from scratch with inspo taken from JC Stress
* At no point in time should the head be null
* */
public class MPMCQueue {
    private final AtomicReference<Node> head;

    public MPMCQueue() {
        this.head = new AtomicReference<>(new Node(new Object()));
    }

    /*
    * We iterate through the queue to reach the tail node
    * When we reach the "last" i.e., the tail, presumed node, we try to CAS to its next, if we fail, we restart from the head
    * If we reach a node who points to itself, we also restart from the head
    * */
    public boolean add(Object o){
        Node node = new Node(Objects.requireNonNull(o));
        Node h = this.head.get();
        Node pred = h;
            for (;  ;){  //Backed by a volatile read
                Node p = pred.lvNext();
                if(p == null && pred.casNext(null, node)){ //CAS'ing null to node is the linearizability point
                    //We have enqueued as the tail
                    return true; //return
                } else if(pred == p || p == null) {
                    //If the node points to itself, or we reached the tailed and failed to CAS, just restart from head
                    pred = h;
                }else {
                    pred = p;
                }
            }
    }

    /*
     * We get the head node, and loop through till we find a node with where item = o, we then try to cas its item to o, if we pass, we connect it's previous next to the node's next, then we link the node to itself
     * However, there exists a point where another thread might snapshot next and see its original "next node" not itself, that is harmless since at the point of snapshot, it points to an actual node in the queue
     *
     * A node is said to be removed from the queue when we've swapped its object to null, after removal, we point the node's next field to itself
     * If we come across a node that points to itself, we restart from the head
     *
     *
     * */
    public boolean remove(Object o){
        if (o == null) return false;
        Node h = this.head.get();

        outer: for (;;){
            Node pred = null; //Node before p
            Node p = h; //Current node, head is always a sentinel, so it will never change, saves us volatile reads
            Node n = p.lvNext();
            for (; ; pred = p, p = n, n = p.lvNext()){  //No NPE with pred can occur since the head is always sentineled
                Object c = p.lvObject();
                if(c != null && Objects.equals(o, c) && p.casObject(c, null)){
                    //If n is not a dead node, we walk forward till we see a live node or reach the tail
                    for (Node r = p ; ; r = n,  n = p.lvNext()){
                        if (n == null || n.lvObject() != null){ // if n is the tail or the next active node
                            //If pred is dead, or it has been unlinked, it means another node has probably linked itself to us
                            // So don't unlink our node, though we're dead
                            if (pred.lvObject() != null && pred.casNext(p, n)){ //if pred is still active or is still linked to p
                                p.soNext(p); //Link to ourselves
                            }

                            return true;
                        }

                        if (r == n) {
                            return true; //Just return
                        }
                    }

                }else if(p == n) {
                    continue outer;
                }else if (n == null) return false;


            }
        }
    }

    /*
    * To implement this, we read the "next" head node, since the head ideally can't be removed and is a dummy node
     * If the head.next node is not a dead node, we read the value, we try cas its item as null,
     *      if we succeed the item cas, we then try to CAS head.next from this node to the next alive node
     *      else we reread head and retry
     * else if the head.next node is a dead node, we iterate until we find the next live node, or we reach the tail
     *      we then read the value of the node and try cas head.next to that node
     *
     * if we encounter a node that points to itself at any point in time here, we restart from head
    * */
    public Object poll(){
        Node h = this.head.get();
        Object o;
        Node p = h.lvNext(); //Here we initially start from the head's next
        for (; p != null;){
                Node q = p.lvNext();
                if ((o = p.lvObject()) != null && p.casObject(o, null)){ //Linearizability point
                    for (Node c = q, b = p;  ; b = c,  c = b.lvNext()){
                        if (c == null || c.lvObject() != null){
                            if(h.casNext(p, c)) {
                                p.soNext(p);
                            } //If we fail to cas next from p to o, that means p has been unattached by another node

                            return o;
                        }

                        if (b == c) return o;
                    }
                }else if (p == q){
                    p = h.lvNext(); //H is always a sentinel so we can just reread its next value since thats the actual head
                }else {
                    p = q; //If p is a dead node just move forward
                }

        }

        return null;
    }

    public boolean contains(Object o){
        if (o == null) return false;
        Node h = this.head.get();
        for (Node p = h.lvNext(); p != null ; ){
            Node q = p.lvNext();
            if (o.equals(p.lvObject())){
                return true;
            }else if (q == p) {
                p = h.lvNext();
            }else p = q;
        }

        return false;
    }

    public boolean canReachTail(){
        Node h = this.head.get();
        for (Node p = h.lvNext(); p != null ; ){
            Node q = p.lvNext();
            if (q == p) {
                p = h.lvNext();
            }else p = q;
        }

        return true;
    }

    static class Node {
       final AtomicReference<Object> o;
       final AtomicReference<Node> next;

        public Node(Object o) {
            this.o = new AtomicReference<>(o);
            this.next = new AtomicReference<>();
        }

        public Node lvNext(){
            return next.getAcquire();
        }

        public boolean casNext(Node old, Node node){
            return next.compareAndSet(old, node);
        }

        public void soNext(Node next){
            this.next.setRelease(next);
        }

        public boolean casObject(Object old, Object obj){
            return o.compareAndSet(old, obj);
        }

        public Object lvObject(){
            return o.getAcquire();
        }
    }
}
