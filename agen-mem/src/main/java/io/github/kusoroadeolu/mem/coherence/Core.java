package io.github.kusoroadeolu.mem.coherence;

import java.util.concurrent.CompletableFuture;

public class Core {
    private final CoreId coreId;
    private final CacheController controller;

    public Core(CoreId coreId) {
        this.coreId = coreId;
        this.controller = new CacheController();
    }

    //Synchronized to enforce sequential invariant
    public synchronized boolean store(MemoryLocation location, Object o){
        CompletableFuture<Boolean> co = new CompletableFuture<>();
        controller.write(location, o, co);
        return co.join();
    }

    public synchronized Object load(MemoryLocation location){
        CompletableFuture<Object> co = new CompletableFuture<>();
        controller.read(location, co);
        return co.join();
    }

    public CoreId coreId() {
        return coreId;
    }
}
