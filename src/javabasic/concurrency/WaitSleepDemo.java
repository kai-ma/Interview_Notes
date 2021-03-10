package javabasic.concurrency;

public class WaitSleepDemo {
    public static void main(String[] args) {
//        verifyWait();
        verifySleep();
    }

    /**
     * 验证wait会释放锁。
     * A先执行，进入到synchronized代码块，获取到锁。B后执行，阻塞。
     * 此时A调用object.wait(1000)，释放锁，B获取到锁并执行任务。
     */
    private static void verifyWait() {
        System.out.println("-----verifyWait-----");
        final Object lock = new Object();
        new Thread(() -> {
            System.out.println("thread A is waiting to get lock");
            synchronized (lock) {
                try {
                    System.out.println("thread A get lock");
                    Thread.sleep(20);
                    System.out.println("thread A do wait method");
                    lock.wait(1000);
                    System.out.println("thread A is done");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "Thread A").start();
        //等待10ms，让ThreadA先开始执行。在ThreadA sleep20ms时间到达之前开始B。
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        new Thread(() -> {
            System.out.println("thread B is waiting to get lock");
            synchronized (lock) {
                try {
                    System.out.println("thread B get lock");
                    System.out.println("thread B is sleeping 10 ms");
                    Thread.sleep(10);  //模拟执行任务
                    System.out.println("thread B is done");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "Thread B").start();
    }

    /**
     * 验证sleep不会释放锁。
     * A先执行，进入到synchronized代码块，获取到锁。B后执行，阻塞。
     * 此时A调用Thread.sleep(1000)，睡眠1s，不会释放锁。
     * 等到A执行完任务，B才会获取到锁并执行任务。
     */
    private static void verifySleep() {
        System.out.println("-----verifySleep-----");
        final Object lock = new Object();
        new Thread(() -> {
            System.out.println("thread A is waiting to get lock");
            synchronized (lock) {
                try {
                    System.out.println("thread A get lock");
                    Thread.sleep(20);
                    System.out.println("thread A sleep 1000ms");
                    Thread.sleep(1000);
                    System.out.println("thread A is done");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "Thread A").start();
        //等待10ms，让ThreadA先开始执行。在ThreadA sleep20ms时间到达之前开始B。
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        new Thread(() -> {
            System.out.println("thread B is waiting to get lock");
            synchronized (lock) {
                try {
                    System.out.println("thread B get lock");
                    System.out.println("thread B is sleeping 10 ms");
                    Thread.sleep(10);  //模拟执行任务
                    System.out.println("thread B is done");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "Thread B").start();
    }
}
