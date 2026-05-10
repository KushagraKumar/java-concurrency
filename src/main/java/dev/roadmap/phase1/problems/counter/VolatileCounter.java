package dev.roadmap.phase1.problems.counter;

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

