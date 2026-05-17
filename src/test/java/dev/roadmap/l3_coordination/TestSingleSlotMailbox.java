package dev.roadmap.l3_coordination;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;

class TestSingleSlotMailbox {

    @Test
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    void testBasicPutTake() throws InterruptedException {
        L3_SingleSlotMailbox<String> mailbox = new L3_SingleSlotMailbox<>();
        mailbox.put("Hello");
        assertEquals("Hello", mailbox.take());
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testProducerBlocksWhenFull() throws InterruptedException {
        L3_SingleSlotMailbox<Integer> mailbox = new L3_SingleSlotMailbox<>();
        mailbox.put(1); // Fill it

        CountDownLatch secondPutStarted = new CountDownLatch(1);
        Thread producer = new Thread(() -> {
            try {
                secondPutStarted.countDown();
                mailbox.put(2); // This should block
            } catch (InterruptedException ignored) {}
        });

        producer.start();
        secondPutStarted.await();
        Thread.sleep(200); // Give it time to block

        assertTrue(producer.isAlive(), "Producer should be blocked while mailbox is full");
        
        assertEquals(1, mailbox.take()); // Clear it
        producer.join(); // Now producer should finish
        assertEquals(2, mailbox.take());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testMultiThreadedContention() throws InterruptedException {
        int pairs = 5;
        int msgsPerThread = 1000;
        L3_SingleSlotMailbox<Integer> mailbox = new L3_SingleSlotMailbox<>();
        List<Integer> results = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch done = new CountDownLatch(pairs * 2);

        // Start Consumers
        for (int i = 0; i < pairs; i++) {
            new Thread(() -> {
                try {
                    for (int j = 0; j < msgsPerThread; j++) {
                        results.add(mailbox.take());
                    }
                } catch (InterruptedException ignored) {}
                finally { done.countDown(); }
            }).start();
        }

        // Start Producers
        for (int i = 0; i < pairs; i++) {
            new Thread(() -> {
                try {
                    for (int j = 0; j < msgsPerThread; j++) {
                        mailbox.put(j);
                    }
                } catch (InterruptedException ignored) {}
                finally { done.countDown(); }
            }).start();
        }

        assertTrue(done.await(8, TimeUnit.SECONDS), "Test timed out under contention");
        assertEquals(pairs * msgsPerThread, results.size());
    }

    @Test
    void testInterruption() throws InterruptedException {
        L3_SingleSlotMailbox<String> mailbox = new L3_SingleSlotMailbox<>();
        AtomicReference<Boolean> interrupted = new AtomicReference<>(false);

        Thread t = new Thread(() -> {
            try {
                mailbox.take(); // Should block indefinitely on empty
            } catch (InterruptedException e) {
                interrupted.set(true);
            }
        });

        t.start();
        Thread.sleep(200);
        t.interrupt();
        t.join(1000);

        assertTrue(interrupted.get(), "Mailbox should propagate InterruptedException");
    }
}
