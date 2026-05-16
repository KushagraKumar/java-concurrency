package dev.roadmap.l2_safety.counters;

public class SynchronizedMethodBasicCounter implements Counter {
    private int value;

    public SynchronizedMethodBasicCounter() {
        this.value = 0;
    }

    @Override
    public synchronized void increment() {
        this.value++;
    }

    @Override
    public int get() {
        return this.value;
    }
}
