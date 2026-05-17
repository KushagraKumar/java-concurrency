package dev.roadmap.l1_basics;

import java.util.concurrent.atomic.AtomicInteger;

public class L1_CooperativeCancellation {
    public static void main(String[] args) throws InterruptedException {
        AtomicInteger openResources = new AtomicInteger(0);

        Thread t = new Thread(() -> {
            openResources.incrementAndGet();

            try {
                for (int i=0;i<=10000;i++) {
                    if (Thread.currentThread().isInterrupted()) {
                        return;
                    }

                    System.out.println("Working number: " + i);
                    Thread.sleep(10);
                }
            } catch (InterruptedException e) {
                System.out.println("Thread was interrupted");
                Thread.currentThread().interrupt();
            } finally {
                openResources.decrementAndGet();
                System.out.println("Resources cleaned up. Open Resources : " + openResources.get() );
            }
        });

        t.start();
        Thread.sleep(500);
        System.out.println("Main: Requesting cancellation");

        t.interrupt();

        t.join();
        System.out.println("Main: worker finished. Final open resources : " + openResources.get());
    }
}
