package io.github.kusoroadeolu.mem.falsesharing;

import java.util.concurrent.ThreadLocalRandom;

public class Counter implements Accum{
    private volatile int a;
    private volatile int b;


    public int setA(int a) {
        this.a = a * ThreadLocalRandom.current().nextInt() & ThreadLocalRandom.current().nextInt() / ThreadLocalRandom.current().nextInt();
        return this.a;

    }

    public int setB(int b) {
        this.b = b * ThreadLocalRandom.current().nextInt() & ThreadLocalRandom.current().nextInt() / ThreadLocalRandom.current().nextInt();
        return this.b;
    }
}
