package io.github.kusoroadeolu.mem.coherence;


/**
 * A cache is divided into two caches, a private data cache or an LLC (last level cache)
 * A cache should have a cache controller and a max size and a current size
 * For the sake of this, the size represents the number of memory locations currently sitting in the cache.
 * For now lets focus on coherence not eviction
 * */
public interface Cache {
    CacheController controller();
    void readFromMainMemory(MemoryLocation location);
    void readRawFromMainMemory(MemoryLocation location);
    void writeToMainMemory(MemoryLocation location, Object value);
}
