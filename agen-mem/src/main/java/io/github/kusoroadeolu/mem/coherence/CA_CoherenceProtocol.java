package io.github.kusoroadeolu.mem.coherence;

import java.util.UUID;

public class CA_CoherenceProtocol implements CoherenceProtocol{

    @Override
    public void ensureWriteVisibility(MemoryLocation location) {
        var cores = MultiProcessorChip.chip().coreIds();
        for (UUID coreId : cores){
            Core core = IntercommunicationNetwork.getCore(coreId);
            core.cacheController().readRawFromMainMemory(location);
        }
    }
}
