package javabasic.reflect;

/**
 * 验证loadClass和forName的区别：forName会初始化，执行静态语句块，赋值类变量，loadClass不会。
 */
public class LoadDifference {
    public static void main(String[] args) throws ClassNotFoundException {
        ClassLoader defaultClassLoader = Robot.class.getClassLoader();
        Class c = defaultClassLoader.loadClass("javabasic.reflect.Robot");
        Class.forName("javabasic.reflect.Robot");
        Thread.yield();
    }
}
