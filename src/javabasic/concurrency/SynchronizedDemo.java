package javabasic.concurrency;

/**
 * 探索synchronized的实现原理，用javap —verbose 反编译字节码文件
 * 编译命令：javac src/javabasic/concurrency/SynchronizedDemo.java
 * 反编译命令： javap -verbose src/javabasic/concurrency/SynchronizedDemo
 * <p>
 * 通过阅读反编译的汇编指令，可以发现：
 * 1.synchronized同步语句块的实现使用的是monitorenter和monitorexit指令，其
 * 中 monitorenter指令指向同步代码块的开始位置，monitorexit指令则指明同步代码块的结束位置。
 * <p>
 * 2. synchronized修饰的方法的实现使用的是ACC_SYNCHRONIZED标识，
 * 该标识指明了该方法是一个同步方法。JVM通过该ACC_SYNCHRONIZED访问标志
 * 来辨别一个方法是否声明为同步方法，从而执行相应的同步调用。
 * <p>
 * 两者的本质都是对对象监视器monitor的获取。
 */
public class SynchronizedDemo {
    public void syncBlock() {
        synchronized (this) {
            System.out.println("synchronized code block");
        }
    }

    public synchronized void syncMethod() {
        System.out.println("synchronized method");
    }
}
