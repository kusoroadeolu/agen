package io.github.kusoroadeolu.mem.coherence;

public interface CoherenceProtocol {
    void ensureWriteVisibility(MemoryLocation location);
}
