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


//    boolean write(MemoryLocation location, Object value){
//        LockedObject lo = memoryLocations.computeIfAbsent(location, _ -> new LockedObject());
//        boolean held = lo.holdWrite();
//        if (held) lo.setObject(value);
//        return held;
//    }
//
//    Object read(MemoryLocation location){
//        LockedObject lo = memoryLocations.computeIfAbsent(location, _ -> new LockedObject());
//        boolean held = lo.holdRead();
//        if (held) return lo.getObject();
//        else return NONE;
//    }
//
//    Object readRaw(MemoryLocation location){
//        LockedObject lo = memoryLocations.get(location);//If we're reading raw, it means we've been forced to reread during a write or read from another cache
//        boolean held = lo.holdRead();
//        if (held) return lo.getObject();
//        else return NONE;
//    }


}
