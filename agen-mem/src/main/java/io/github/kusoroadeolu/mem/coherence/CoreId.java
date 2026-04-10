package io.github.kusoroadeolu.mem.coherence;


public record CoreId(int id) {
    public static CoreId of(int id){
        return new CoreId(id);
    }
}
