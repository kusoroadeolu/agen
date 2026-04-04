package io.github.kusoroadeolu.mem.coherence;

public class Core {
    private final Cache pdc;

    public Core() {
        this.pdc = new PrivateDataCache();
    }

    public CacheController cacheController() {
        return pdc.controller();
    }
}
