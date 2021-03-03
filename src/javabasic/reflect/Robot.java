package javabasic.reflect;

public class Robot {
//    private String name;

//    public void sayHi(String helloSentence) {
//        System.out.println(helloSentence + " " + name);
//    }
//
//    private String throwHello(String tag) {
//        return "Hello " + tag;
//    }

    // 初始化过程中会执行静态代码块
    static {
        System.out.println("Hello Robot in static");
    }
}
