package io.github.kusoroadeolu.mem.coherence;

import java.util.Objects;

public class Bus<T> {

    private T value;
    public Bus(){}

    public boolean offer(T o){
        Objects.requireNonNull(o);
        synchronized (this){
            if (value == null){
                value = o;
                return true;
            }

            return false;
        }
    }

    public T poll(){
        synchronized (this){
            var o = value;
            value = null;
            return o;
        }
    }

    public T peek(){
        synchronized (this){
            return value;
        }
    }
}
