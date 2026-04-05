package io.github.kusoroadeolu.mem.coherence;

import java.util.concurrent.CountDownLatch;

public class AckMessage {
    private final CountDownLatch acks;
    private final MemoryLocation location;

    public AckMessage(MemoryLocation location) {
        this.acks = new CountDownLatch(MultiProcessorChip.chip().cores().size() - 1);
        this.location = location;
    }
    public AckMessage() {
        this(null);
    }


    public MemoryLocation location() {
        return location;
    }

    public void acknowledge(){
        acks.countDown();
    }

    public void waitAcknowledgement() throws InterruptedException{
        acks.await();
    }
}