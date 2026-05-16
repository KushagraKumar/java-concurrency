package dev.roadmap.l3_coordination;

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
    private boolean full = false;

    public synchronized void put(T msg) throws InterruptedException {
        // TODO: Wait while full, then put
    }

    public synchronized T take() throws InterruptedException {
        // TODO: Wait while empty, then take
        return null;
    }
}
