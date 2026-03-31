package io.github.kusoroadeolu.mem.coherence;

public class PrivateDataCacheController implements CacheController{
    private final Cache cache;

    public PrivateDataCacheController(Cache cache){
        this.cache = cache;
    }

    //Synchronized to enforce the sequential invariant

    @Override
    public synchronized void readFromMainMemory(MemoryLocation location) {
        cache.readFromMainMemory(location);
    }

    @Override
    public synchronized void readRawFromMainMemory(MemoryLocation location) {
        cache.readRawFromMainMemory(location);
    }

    @Override
    public synchronized void writeToMainMemory(MemoryLocation location, Object value) {
        cache.writeToMainMemory(location, value);
    }
}
