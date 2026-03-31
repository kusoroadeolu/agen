package io.github.kusoroadeolu.mem.coherence;

//Communication between caches. A cache can read directly from main memory without a cache controller
public interface CacheController {
    void readFromMainMemory(MemoryLocation location);

    void readRawFromMainMemory(MemoryLocation location);

    void writeToMainMemory(MemoryLocation location, Object value);
}
