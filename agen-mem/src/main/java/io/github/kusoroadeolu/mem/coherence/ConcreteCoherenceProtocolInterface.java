package io.github.kusoroadeolu.mem.coherence;

import static io.github.kusoroadeolu.mem.coherence.MultiProcessorChip.chip;

public class ConcreteCoherenceProtocolInterface implements CoherenceProtocolInterface{
    public final static Object NONE = new Object();
    public final CoherenceProtocol protocol;

    public ConcreteCoherenceProtocolInterface() {
        this.protocol = new CA_CoherenceProtocol();
    }

    //Read then force other cores to read as well
    @Override
    public Object readRequest(MemoryLocation location) {
        LockedObject object = chip().mainMemory().get(location);
        if (object.tryHoldRead()){
            var var = object.getValue();
            this.protocol.forceRead(location);
            object.releaseRead();
            return var;
        }

        return NONE;
    }

    //Write then force other cores to read your write
    @Override
    public boolean writeRequest(MemoryLocation location, Object valueToBeWritten) {
        LockedObject object = chip().mainMemory().get(location);
        if (object.tryHoldWrite()){
            object.setValue(valueToBeWritten);
            this.protocol.makeWriteVisible(location);
            object.releaseWrite();
            return true;
        }

        return false;
    }
}
