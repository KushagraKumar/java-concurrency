package dev.roadmap.phase1.concepts.basicthread;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ReentrantLockBasicCounter {
    private int value;
    private Lock lock;

    public ReentrantLockBasicCounter() {
        this.value = 0;
        this.lock = new ReentrantLock();
    }

    public void increment() {
        lock.lock();

        try {
            value++;
        } finally {
            this.lock.unlock();
        }
    }

    public int get() {
        return this.value;
    }

}
