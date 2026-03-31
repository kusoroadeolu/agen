package io.github.kusoroadeolu.mem.coherence;

public class PrivateDataCacheController implements CacheController{
    private final Cache cache;

    public PrivateDataCacheController(Cache cache){
        this.cache = cache;
    }

    //Synchronized to enforce the sequential invariant

    @Override
    public synchronized Object readFromMainMemory(MemoryLocation location) {
       return cache.readFromMainMemory(location);
    }

    @Override
    public synchronized Object readRawFromMainMemory(MemoryLocation location) {
        return cache.readRawFromMainMemory(location);
    }

    @Override
    public synchronized boolean writeToMainMemory(MemoryLocation location, Object value) {
        return cache.writeToMainMemory(location, value);
    }
}
