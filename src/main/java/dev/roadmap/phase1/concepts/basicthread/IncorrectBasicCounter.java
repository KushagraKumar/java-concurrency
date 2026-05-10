package dev.roadmap.phase1.concepts.basicthread;

public class IncorrectBasicCounter {
    private int value;

    public IncorrectBasicCounter() {
        this.value = 0;
    }

    public void increment() {
        this.value++;
    }

    public int get() {
        return this.value;
    }
}
