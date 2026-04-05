package io.github.kusoroadeolu.mem.coherence;

import java.util.HashMap;
import java.util.Map;

public class PrivateDataCache {
    private final Map<MemoryLocation, Object> cache;

    public PrivateDataCache() {
        this.cache = new HashMap<>();
    }

    public void write(MemoryLocation location, Object o){
        cache.put(location, o);
    }

    public Object get(MemoryLocation location){
        return cache.get(location);
    }
}
