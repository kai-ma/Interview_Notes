package javabasic.concurrency;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 基本数据类型的共享变量，可以用CAS原理，依赖atomic中的原子类，实现无锁方案
 */
public class CASCase {
    /**
     * 如果要实现线程安全的共享变量count的+1，可以使用synchronized修饰的有锁方案。
     */
    class SynchronizedCase {
        private volatile int count = 0;

        //若要线程安全执行执行count++，需要加锁
        public synchronized void increment() {
            count++;
        }

        public int getCount() {
            return count;
        }
    }

    /**
     * 也可以采用乐观锁的无锁方案
     */
    private AtomicInteger count = new AtomicInteger();

    public void increment() {
        count.incrementAndGet();
    }

    //使用AtomicInteger之后，不需要加锁，也可以实现线程安全。
    public int getCount() {
        return count.get();
    }

}
