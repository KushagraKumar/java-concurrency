package dev.roadmap.phase1.problems.counter;

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
