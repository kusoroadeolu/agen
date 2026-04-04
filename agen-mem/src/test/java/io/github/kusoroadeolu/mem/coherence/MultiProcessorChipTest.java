package io.github.kusoroadeolu.mem.coherence;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;

class MultiProcessorChipTest {

    @Test
    void onWrite_mainMemory_shouldContainWrittenObject(){
        var currentTime = System.currentTimeMillis();
        var memLocation = new MemoryLocation();
        MultiProcessorChip.chip().write(memLocation, currentTime);
        assertEquals(currentTime, MultiProcessorChip.chip().mainMemory().get(memLocation).getCurrentValue());
    }

    @Test
    void onRead_mainMemory_shouldNotContainWrittenObject(){
        var memLocation = new MemoryLocation();
        var value = MultiProcessorChip.chip().read(memLocation);
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
            MultiProcessorChip.chip().write(memLocation, currentTime);
            advance.countDown();
        });

        Thread.startVirtualThread(() -> {
            awaitLatch(wait);
            MultiProcessorChip.chip().read(memLocation);
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