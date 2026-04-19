package io.github.kusoradeolu.agen.expr;

import io.github.kusoradeolu.agen.expr.queues.MPMCQueue;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

//Simple sequential correctness tests
class SimpleMPMCQueueTest {

    @Test
    void shouldSuccessfullyAddNode(){
        MPMCQueue queue = new MPMCQueue();
        queue.add("Hello");
        assertTrue(queue.remove("Hello"));
    }

    @Test
    void shouldReturnFalseIfNoItemAdded(){
        MPMCQueue queue = new MPMCQueue();
        assertFalse(queue.remove("Hello"));
    }

}