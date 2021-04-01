package javabasic.concurrency;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 测试ThreadLocal，ThreadLocal会为每一个线程提供一个独立的变量副本，因此避免了多线程竞争。
 */
public class ThreadLocalDemo implements Runnable {
    private static final ThreadLocal<Integer> threadLocal = ThreadLocal.withInitial(() -> 1);

    @Override
    public void run() {
        System.out.println("Thread Name= " + Thread.currentThread().getName() + " default value = " + threadLocal.get());
        int i = new Random().nextInt(1000);
        try {
            Thread.sleep(i);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        threadLocal.set(i);
        System.out.println("Thread Name= " + Thread.currentThread().getName() + " new value = " + threadLocal.get());
    }


    public static void main(String[] args) throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        for (int i = 0; i < 5; i++) {
            executorService.execute(new ThreadLocalDemo());
            Thread.sleep(new Random().nextInt(1000));
        }
        executorService.shutdown();
    }
}
