package javabasic.concurrency;

public class VolatileDemo {

    volatile boolean shutdown;

    public void close() {
        this.shutdown = true;
    }

    public void dowork() {
        while (!shutdown) {
            System.out.println("safe....");
        }
    }
}
