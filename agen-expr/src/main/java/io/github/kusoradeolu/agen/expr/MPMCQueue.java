package io.github.kusoradeolu.agen.expr;

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
        Objects.requireNonNull(o);
        Node node = new Node(o);

            for (Node p = this.head.get();  ;){  //Backed by a volatile read
                Node n = p.next();
                if(n == null && p.casNext(null, node)){ //CAS'ing null to node is the linearizability point
                    //We have enqueued as the tail
                    return true; //return
                } else if(p == n) {
                    //If the node points to itself, just restart from head
                    p = this.head.get();
                }else {
                    p = n;
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

        outer: for (;;){
            Node pred = null; //Node before p
            Node p = this.head.get(); //Current node
            Node n = p.next();

            for (; ; pred = p, p = n, n = p.next()){  //No NPE with pred can occur since the head is always sentineled
                Object c = p.object();
                if(c != null && Objects.equals(o, c) && p.casObject(c, null)){
                    //If n is not a dead node, we walk forward till we see a live node or reach the tail
                    for (; ; n = p.next()){
                        if (n == null || n.object() != null){ // if n is the tail or the next active node
                            //If pred is dead or it has been unlinked, it means another node has linked itself to us
                            // So don't unlink our node, though we're dead
                            if (pred.object() != null && pred.casNext(p, n)){ //if pred is still active
                                p.setNext(p); //Link to ourselves
                            }

                            return true;
                        }
                    }

                }else if(p == n) {
                    continue outer;
                }else if (n == null) return false;


            }
        }
    }

    public boolean contains(Object o){
        if (o == null) return false;
        Node h = this.head.get();
        for ( ; ; ){
            Node n = h.next();
            if (n == null) return false;
            else if (o.equals(n.object())){
                return true;
            }else if (h == n) {
                h = this.head.get();
            }else h = n;
        }
    }

    static class Node {
       final AtomicReference<Object> o;
       final AtomicReference<Node> next;

        public Node(Object o) {
            this.o = new AtomicReference<>(o);
            this.next = new AtomicReference<>();
        }

        public Node next(){
            return next.get();
        }

        public boolean casNext(Node old, Node node){
            return next.compareAndSet(old, node);
        }

        public void setNext(Node next){
            this.next.setRelease(next);
        }

        public boolean casObject(Object old, Object obj){
            return o.compareAndSet(old, obj);
        }

        public Object object(){
            return o.get();
        }
    }
}
