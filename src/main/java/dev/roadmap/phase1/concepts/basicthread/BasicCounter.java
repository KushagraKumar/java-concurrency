package dev.roadmap.phase1.concepts.basicthread;

public class BasicCounter {
    private int value;

    public BasicCounter() {
        this.value = 0;
    }

    public void increment() {
        this.value++;
    }

    public int get() {
        return this.value;
    }
}
