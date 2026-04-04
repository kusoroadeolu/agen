package io.github.kusoroadeolu.mem.coherence;

import java.util.UUID;

public class IntercommunicationNetwork {
    private IntercommunicationNetwork(){

    }

    public static Core getCore(UUID coreId){
        return MultiProcessorChip.chip().cores().get(coreId);
    }
}
