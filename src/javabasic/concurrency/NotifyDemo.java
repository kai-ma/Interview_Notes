package javabasic.concurrency;

public class NotifyDemo {
    /**
     * volatile 一旦某个线程修改了go的值，对于其他线程都能立即看到相关改动。
     */
    private volatile boolean go = false;

    public static void main(String[] args) throws InterruptedException {
        final NotifyDemo test = new NotifyDemo();

        Runnable waitTask = () -> {
            try {
                test.shouldGo();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(Thread.currentThread().getName() + " finished Execution");
        };

        Runnable notifyTask = () -> {
            test.go();
            System.out.println(Thread.currentThread().getName() + " finished Execution");
        };
        //t1-t4都用的同一个test对象，只有一个能获取到锁
        //t1、t2、t3 wait
        Thread t1 = new Thread(waitTask, "WT1");
        Thread t2 = new Thread(waitTask, "WT2");
        Thread t3 = new Thread(waitTask, "WT3");
        //t4来notify
        Thread t4 = new Thread(notifyTask, "NT1");

        //starting all waiting thread
        t1.start();
        t2.start();
        t3.start();

        //pause to ensure all waiting thread started successfully
        Thread.sleep(200);

        //starting notifying thread
        t4.start();

    }

    /**
     * wait和notify必须在synchronized代码块中被调用
     * t1、t2、t3都会进入while循环，调用wait之后进入等待区，等待被唤醒。
     */
    private synchronized void shouldGo() throws InterruptedException {
        while (!go) {
            System.out.println(Thread.currentThread()
                    + " is going to wait on this object");
            //释放锁并且等待被唤醒
            wait();
            System.out.println(Thread.currentThread() + " is woken up");
        }
        //退出synchronized代码块-释放锁之前重新把go置为false，
        //有这一句，即时notifyAll()，也只有一个wait任务能执行完。没有这一句，notifyAll()时，所有任务都能执行完
        go = false;
    }

    /**
     * shouldGo()和go()都用的同一个对象的锁
     */
    private synchronized void go() {
        while (!go) {
            System.out.println(Thread.currentThread()
                    + " is going to notify all or one thread waiting on this object");
            //修改go变量，让被唤醒的锁能结束while循环。
            go = true;
            //notify();
            notifyAll();
        }
    }
}
