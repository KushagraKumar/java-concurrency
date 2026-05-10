package dev.roadmap.phase1.concepts.basicthread;

public class SynchronizedMethodBasicCounter {
    private int value;

    public SynchronizedMethodBasicCounter() {
        this.value = 0;
    }

    public synchronized void increment() {
        this.value++;
    }

    public int get() {
        return this.value;
    }
}
