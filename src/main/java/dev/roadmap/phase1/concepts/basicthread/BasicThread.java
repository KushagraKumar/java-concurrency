package dev.roadmap.phase1.concepts.basicthread;

public class BasicThread {
    public static void main(String[] args) throws InterruptedException {
        BasicCounter counter = new BasicCounter();

        Thread thread1 = new Thread(() -> {
            for (int i=0;i<1000;i++) counter.increment();
        });

        Thread thread2 = new Thread(() -> {
            for (int i=0;i<1000;i++) counter.increment();
        });

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        System.out.println(counter.get());
    }
}
