package io.github.kusoroadeolu.mem.coherence;

import java.util.*;

public class MultiProcessorChip {
    private final static int CORE_COUNT = Runtime.getRuntime().availableProcessors();
    private final static MultiProcessorChip CHIP = new MultiProcessorChip();
    private final Map<UUID, Core> cores;
    private final MainMemory memory;
    private final CoherenceProtocolInterface coherenceProtocolInterface;

    public MultiProcessorChip() {
        this.memory = new MainMemory();
        var map = new HashMap<UUID, Core>();
        for (int i = 0; i < CORE_COUNT; ++i){
            map.put(UUID.randomUUID(), new Core());
        }

        this.coherenceProtocolInterface = new ConcreteCoherenceProtocolInterface();
        this.cores = Collections.unmodifiableMap(map);
    }

    public MainMemory mainMemory(){
        return memory;
    }

    public Set<UUID> coreIds(){
        return cores.keySet();
    }

    public Map<UUID, Core> cores(){
        return cores;
    }

    public static MultiProcessorChip chip(){
        return CHIP;
    }


    public CoherenceProtocolInterface cpInterface(){
        return coherenceProtocolInterface;
    }

}
