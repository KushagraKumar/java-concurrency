package dev.roadmap.l2_safety.counters;

public class BasicCounter implements Counter {
    private int value;

    public BasicCounter() {
        this.value = 0;
    }

    @Override
    public void increment() {
        this.value++;
    }

    @Override
    public int get() {
        return this.value;
    }
}
