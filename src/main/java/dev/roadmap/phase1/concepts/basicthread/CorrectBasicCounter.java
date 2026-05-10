package dev.roadmap.phase1.concepts.basicthread;

public class CorrectBasicCounter {
    private int value;

    public CorrectBasicCounter() {
        this.value = 0;
    }

    public synchronized void increment() {
        this.value++;
    }

    public int get() {
        return this.value;
    }
}
