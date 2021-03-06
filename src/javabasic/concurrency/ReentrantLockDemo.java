package javabasic.concurrency;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Reentrant使用举例。使用公平锁，两个线程会交替获得到锁。
 * 默认情况下是非公平锁，随机获得到锁。
 */
public class ReentrantLockDemo implements Runnable {
    /**
     * 默认是非公平锁。
     */
//    ReentrantLock lock = new ReentrantLock();
    /**
     * 参数true为公平锁。
     */
    ReentrantLock lock = new ReentrantLock(true);


    @Override
    public void run() {
        while (true) {
            lock.lock();
            try {
                System.out.println(Thread.currentThread().getName() + " get lock");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }
    }

    public static void main(String[] args) {
        ReentrantLockDemo reentrantLockDemo = new ReentrantLockDemo();
        Thread thread1 = new Thread(reentrantLockDemo);
        Thread thread2 = new Thread(reentrantLockDemo);
        thread1.start();
        thread2.start();
    }
}
