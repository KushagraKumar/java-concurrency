package dev.roadmap.l2_safety.counters;

public class VolatileCounter implements Counter {
    private volatile int value = 0;

    @Override
    public void increment() {
        this.value++;
    }

    @Override
    public int get() {
        return this.value;
    }
}

