package io.github.kusoroadeolu.mem.falsesharing;

import java.util.concurrent.ThreadLocalRandom;

public class PaddedCounter implements Accum{
    private volatile int a;
    private long l1, l2, l3, l4, l5, l6, l7, l8;
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
