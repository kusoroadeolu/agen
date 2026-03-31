package io.github.kusoroadeolu.mem.coherence;

import java.util.HashMap;
import java.util.Map;

import static io.github.kusoroadeolu.mem.coherence.ConcreteCoherenceProtocolInterface.NONE;
import static io.github.kusoroadeolu.mem.coherence.MultiProcessorChip.chip;


//A sequential cache
public class PrivateDataCache implements Cache{
    private final Map<MemoryLocation, Object> cacheMap;
    private final PrivateDataCacheController controller;

    public PrivateDataCache() {
        this.cacheMap = new HashMap<>();
        this.controller = new PrivateDataCacheController(this);
    }

    @Override
    public Object readFromMainMemory(MemoryLocation location) {
        var object = MultiProcessorChip.chip().cpInterface().readRequest(location);
        if (object != NONE){ //If we were actually able to read the value
            cacheMap.put(location, object);
        }

        return object;
    }

    //Basically during a readwrite epoch, we can read raw from main memory
    @Override
    public Object readRawFromMainMemory(MemoryLocation location) {
        LockedObject object = chip().mainMemory().get(location);
        cacheMap.put(location, object);
        return object;
    }

    @Override
    public boolean writeToMainMemory(MemoryLocation location, Object value) {
        return MultiProcessorChip.chip().cpInterface().writeRequest(location, value);
    }

    @Override
    public CacheController controller() {
        return controller;
    }
}
