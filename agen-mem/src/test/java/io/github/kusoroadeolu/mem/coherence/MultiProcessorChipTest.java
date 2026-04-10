package io.github.kusoroadeolu.mem.coherence;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;

class MultiProcessorChipTest {

    @Test
    void onWrite_mainMemory_shouldContainWrittenObject(){
        var currentTime = System.currentTimeMillis();
        var memLocation = new MemoryLocation();
        var stored = MultiProcessorChip.chip().store(memLocation, currentTime);
        assertTrue(stored);
        assertEquals(currentTime, MultiProcessorChip.chip().memory().get(memLocation).getCurrentValue());
    }

    @Test
    void onRead_mainMemory_shouldNotContainWrittenObject(){
        var memLocation = new MemoryLocation();
        var value = MultiProcessorChip.chip().load(memLocation);
        assertNull(value);
    }


    @Test
    void simpleCountDownLatchTestToTestDoesNotDeadlockUnderNormalInterLeavings(){
        CountDownLatch wait = new CountDownLatch(1);
        CountDownLatch advance = new CountDownLatch(2);
        var currentTime = System.currentTimeMillis();
        var memLocation = new MemoryLocation();

        Thread.startVirtualThread(() -> {
            awaitLatch(wait);
            MultiProcessorChip.chip().store(memLocation, currentTime);
            advance.countDown();
        });

        Thread.startVirtualThread(() -> {
            awaitLatch(wait);
            MultiProcessorChip.chip().load(memLocation);
            advance.countDown();
        });

        wait.countDown();
        awaitLatch(advance);
    }


    void awaitLatch(CountDownLatch latch){
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}