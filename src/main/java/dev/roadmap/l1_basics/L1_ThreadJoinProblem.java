package dev.roadmap.l1_basics;

/**
 * L1: Thread Joining Problem
 * 
 * Objective: Start 3 worker threads that "do work" (e.g., print a message and sleep for 100ms).
 * Ensure the main thread waits for ALL 3 workers to finish before printing "All workers done".
 * 
 * Concepts: Thread.start(), Thread.join()
 */
public class L1_ThreadJoinProblem {
    public static void main(String[] args) throws InterruptedException {
        // TODO: Start 3 threads and wait for them

        Thread t1 = new Thread(() -> {
            System.out.println("Thread one working!");
        });

        Thread t2 = new Thread(() -> {
            System.out.println("Thread two working!");
        });

        Thread t3 = new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("Thread t3 working");
        });


        t1.start();
        t2.start();
        t3.start();

        t1.join();
        t2.join();
        t3.join();


        System.out.println("All workers done");
    }
}
