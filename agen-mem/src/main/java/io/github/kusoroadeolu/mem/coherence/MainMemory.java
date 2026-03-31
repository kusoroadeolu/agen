package io.github.kusoroadeolu.mem.coherence;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class MainMemory {
    private final ConcurrentMap<MemoryLocation, LockedObject> memoryLocations;
    private static final Object NONE = new Object(); //Write lock is held

    public MainMemory() {
        this.memoryLocations = new ConcurrentHashMap<>();
    }

    public LockedObject get(MemoryLocation location){
        return memoryLocations.computeIfAbsent(location, _ -> new LockedObject());
    }
}
