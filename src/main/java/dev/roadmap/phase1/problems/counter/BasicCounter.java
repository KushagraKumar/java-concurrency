package dev.roadmap.phase1.problems.counter;

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
