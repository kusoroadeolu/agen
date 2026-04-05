package io.github.kusoroadeolu.mem.coherence;

public interface CacheCoherenceProtocol {
    Object readRequest(MemoryLocation location);
    boolean writeRequest(MemoryLocation location, Object value);
}
