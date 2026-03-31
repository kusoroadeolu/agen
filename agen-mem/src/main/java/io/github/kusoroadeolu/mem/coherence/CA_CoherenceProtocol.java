package io.github.kusoroadeolu.mem.coherence;

import java.util.UUID;

public class CA_CoherenceProtocol implements CoherenceProtocol{
    @Override
    public void makeWriteVisible(MemoryLocation location) {
        var cores = MultiProcessorChip.chip().coreIds();
        for (UUID coreId : cores){
            Core core = IntercommunicationNetwork.getCore(coreId);
            core.controller().readRawFromMainMemory(location);
        }
    }

    @Override
    public void forceRead(MemoryLocation location) {
        var cores = MultiProcessorChip.chip().coreIds();
        for (UUID coreId : cores){
            Core core = IntercommunicationNetwork.getCore(coreId);
            core.controller().readFromMainMemory(location);
        }
    }
}
