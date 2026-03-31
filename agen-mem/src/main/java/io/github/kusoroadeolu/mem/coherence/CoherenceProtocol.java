package io.github.kusoroadeolu.mem.coherence;

public interface CoherenceProtocol {
    void makeWriteVisible(MemoryLocation location);

    void forceRead(MemoryLocation location);
}
