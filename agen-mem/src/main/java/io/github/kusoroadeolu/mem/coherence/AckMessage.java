package io.github.kusoroadeolu.mem.coherence;

import java.util.concurrent.CountDownLatch;

public class AckMessage {
    private final CountDownLatch ack;
    private final MemoryLocation location;

    public AckMessage(MemoryLocation location) {
        this.ack = new CountDownLatch(MultiProcessorChip.chip().cores().size() - 1);
        this.location = location;
    }
    public AckMessage() {
        this(null);
    }


    public MemoryLocation location() {
        return location;
    }

    public void acknowledge(){
        ack.countDown();
    }

    public void waitAcknowledgement() throws InterruptedException{
        ack.await();
    }
}