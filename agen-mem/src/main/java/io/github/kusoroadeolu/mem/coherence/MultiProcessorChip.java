package io.github.kusoroadeolu.mem.coherence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class MultiProcessorChip {
    private final MainMemory memory;
    private final InterConnectionNetwork network;
    private final CacheCoherenceProtocol protocol;
    private final List<Core> cores;
    private static final int CORE_COUNT = Runtime.getRuntime().availableProcessors();

    public MultiProcessorChip() {
        this.memory = new MainMemory();
        this.cores = createCores();
        this.network = InterConnectionNetwork.getInstance();
        protocol = new CA_CoherenceProtocol();

    }

    private List<Core> createCores() {
        var list = new ArrayList<Core>();
        for (int i = 0; i < CORE_COUNT; ++i){
            list.add(i, new Core(CoreId.of(i)));
        }
        return Collections.unmodifiableList(list);
    }


    public boolean store(MemoryLocation location, Object o){
        int random = ThreadLocalRandom.current().nextInt(CORE_COUNT);
        return cores.stream()
                .filter(c -> c.coreId().id() == random)
                .toList()
                .getFirst()
                .store(location, o);
    }

    public Object load(MemoryLocation location){
        int random = ThreadLocalRandom.current().nextInt(CORE_COUNT);
        return cores.stream()
                .filter(c -> c.coreId().id() == random)
                .toList()
                .getFirst()
                .load(location);
    }

    public static MultiProcessorChip chip(){
        return ChipHolder.CHIP;
    }

    public List<Core> cores(){
        return cores;
    }

    public MainMemory memory(){
        return memory;
    }

    public CacheCoherenceProtocol protocol() {
        return protocol;
    }


    private static class ChipHolder{
        private final static MultiProcessorChip CHIP = new MultiProcessorChip();
    }
}
