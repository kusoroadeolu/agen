package io.github.kusoroadeolu.mem.coherence;

import java.util.UUID;

public class MemoryLocation {
    private final UUID location;

    public MemoryLocation() {
        this.location = UUID.randomUUID();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        MemoryLocation that = (MemoryLocation) o;
        return location.equals(that.location);
    }

    @Override
    public int hashCode() {
        return location.hashCode();
    }
}
