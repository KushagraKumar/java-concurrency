package dev.roadmap.l3_coordination;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * L3: Single-Slot Mailbox (Wait/Notify)
 * 
 * Objective: Implement a thread-safe "Mailbox" that can hold exactly one message.
 * - put(msg): If the box is full, wait until it is empty. Then place the message and notify.
 * - take(): If the box is empty, wait until it is full. Then take the message and notify.
 * 
 * Concepts: synchronized blocks, Object.wait(), Object.notifyAll()
 */
public class L3_SingleSlotMailbox<T> {
    private T message;
    private volatile boolean full = false;

    public synchronized void put(T msg) throws InterruptedException {
        while(full) {
            this.wait();
        }

        // Mailbox is empty
        this.message = msg;
        full = true;
        this.notifyAll();
    }

    public synchronized T take() throws InterruptedException {
        while(!full) {
            this.wait();
        }

        T msg = this.message;
        full = false;
        this.notifyAll();
        return msg;
    }
}
