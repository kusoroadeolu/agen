package io.github.kusoroadeolu.mem.coherence;

public interface CoherenceProtocolInterface {
    Object readRequest(MemoryLocation location);
    //IDK what we'll use the bool ack for but yeah
    boolean writeRequest(MemoryLocation location, Object valueToBeWritten);
}
