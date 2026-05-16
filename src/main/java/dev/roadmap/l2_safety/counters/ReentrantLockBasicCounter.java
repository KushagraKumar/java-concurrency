package dev.roadmap.l2_safety.counters;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ReentrantLockBasicCounter implements Counter {
    private int value;
    private final Lock lock;

    public ReentrantLockBasicCounter() {
        this.value = 0;
        this.lock = new ReentrantLock();
    }

    @Override
    public void increment() {
        lock.lock();

        try {
            value++;
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public int get() {
        return this.value;
    }

}
