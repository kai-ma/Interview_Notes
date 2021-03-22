# Java关键字

| 访问控制             | private  | protected  | public   |              |            |           |        |
| -------------------- | -------- | ---------- | -------- | ------------ | ---------- | --------- | ------ |
| 类，方法和变量修饰符 | abstract | class      | extends  | final        | implements | interface | native |
|                      | new      | static     | strictfp | synchronized | transient  | volatile  |        |
| 程序控制             | break    | continue   | return   | do           | while      | if        | else   |
|                      | for      | instanceof | switch   | case         | default    |           |        |
| 错误处理             | try      | catch      | throw    | throws       | finally    |           |        |
| 包相关               | import   | package    |          |              |            |           |        |
| 基本类型             | boolean  | byte       | char     | double       | float      | int       | long   |
|                      | short    | null       | true     | false        |            |           |        |
| 变量引用             | super    | this       | void     |              |            |           |        |
| 保留字               | goto     | const      |          |              |            |           |        |



## 访问权限控制

| 修饰词     | 同一个包的类 | 继承类 | 其他类 |
| ---------- | ------------ | ------ | ------ |
| private    | ×            | ×      | ×      |
| 无（默认） | √            | ×      | ×      |
| protected  | √            | √      | ×      |
| public     | √            | √      | √      |

[注意protected](https://blog.51cto.com/zhangjunhd/19287 )

1. 父类的protected成员是包内可见的，并且对子类可见；
2. 若子类与父类不在同一包中，那么在子类中，子类实例可以访问其从父类继承而来的protected方法，而不能访问父类实例的protected方法。

```java
package 1
class MyObject3 {
protected Object clone() throws CloneNotSupportedException {
       return super.clone();
    }
}
 
package 2
public class Test3 extends MyObject3 {
    public static void main(String args[]) {
       MyObject3 obj = new MyObject3();
       obj.clone(); // Compile error.  不能通过父类访问
       Test3 tobj = new Test3();
       tobj.clone();// Complie OK.  可以继承
    }
}
```

Class类的构造方法是private，只有JVM能创建Class实例，我们自己的Java程序是无法创建Class实例的。



## final

final关键字主要用在三个地方：变量、方法、类。

1. 对于一个final变量，如果是基本数据类型的变量，则其数值一旦在初始化之后便不能更改；如果是引用类型的变量，则在对其初始化之后便不能再让其指向另一个对象。
2. 当用final修饰一个类时，表明这个类不能被继承。final类中的所有成员方法都会被隐式地指定为final方法。
3. 使用final方法的原因有两个。第一个原因是把方法锁定，以防任何继承类修改它的含义；第二个原因是效率。在早期的Java实现版本中，会将final方法转为内嵌调用。但是如果方法过于庞大，可能看不到内嵌调用带来的任何性能提升（现在的Java版本已经不需要使用final方法进行这些优化了）。类中所有的private方法都隐式地指定为final。

**final 修饰一个对象，能否调用对象修改属性的方法？**

final 修饰的是对应的引用，这意味着引用不可改变，即不可重新赋值，但**对象内部的成员是可变的**  



## static

`static`关键字主要有以下四种使用场景：

1. 修饰成员变量和成员方法: 被`static`修饰的成员属于类，不属于单个这个类的某个对象，被类中所有对象共享，可以并且建议通过类名调用。被`static`声明的成员变量属于静态成员变量，静态变量存放在`Java`内存区域的**方法区。注意：静态方法不能被重写。** 调用格式：类名.静态变量名 类名.静态方法名()。
2. 静态代码块: 静态代码块定义在类中方法外, 静态代码块在非静态代码块之前执行(静态代码块—>非静态代码块—>构造方法)。 该类不管创建多少对象，静态代码块只执行一次。**在类加载的准备阶段就会执行**
3. 静态内部类（static修饰类的话只能修饰内部类）： 静态内部类与非静态内部类之间存在一个最大的区别: 非静态内部类在编译完成之后会隐含地保存着一个引用，该引用是指向创建它的外围类，但是静态内部类却没有。没有这个引用就意味着：1. 它的创建是不需要依赖外围类的创建。2. 它不能使用任何外围类的非static成员变量和方法。使用例子：jdk.internal.loader.ClassLoaders中静态内部类定义的各种类加载器
4. 静态导包(用来导入类中的静态资源，1.5之后的新特性): 格式为：import static 这两个关键字连用可以指定导入某个类中的指定静态资源，并且不需要使用类名调用类中静态成员，可以直接使用类中静态成员变量和成员方法。



拓展内容：更多关于方法区的内容在JVM部分。

方法区与 Java 堆一样，是各个线程共享的内存区域，它用于存储已被虚拟机加载的类信息、常量、静态变量、即时编译器编译后的代码等数据。虽然Java虚拟机规范把方法区描述为堆的一个逻辑部分，但是它却有一个别名叫做 Non-Heap（非堆），目的应该是与 Java 堆区分开来。

HotSpot 虚拟机中方法区也常被称为 “永久代”，本质上两者并不等价。仅仅是因为 HotSpot 虚拟机设计团队用永久代来实现方法区而已，这样 HotSpot 虚拟机的垃圾收集器就可以像管理 Java 堆一样管理这部分内存了。但是这并不是一个好主意，因为这样更容易遇到内存溢出问题。因此在Java8之后取消了永久代，改为了元空间。



### 静态代码块与非静态代码块

static{}静态代码块与{}非静态代码块(构造代码块)

相同点： 都是在JVM加载类时且在构造方法执行之前执行，在类中都可以定义多个，定义多个时按定义的顺序执行，一般在代码块中对一些static变量进行赋值。

不同点： 静态代码块在非静态代码块之前执行(静态代码块—非静态代码块—构造方法)。静态代码块只在第一次new执行一次，之后不再执行，而非静态代码块在每new一次就执行一次。 非静态代码块可在普通方法中定义(不过作用不大)；而静态代码块不行。

一般情况下，如果有些代码比如一些项目最常用的变量或对象必须在项目启动的时候就执行的时候，需要使用静态代码块，这种代码是主动执行的。如果我们想要设计不需要创建对象就可以调用类中的方法，例如：Arrays类，Character类，String类等，就需要使用静态方法, 两者的区别是**静态代码块是自动执行的而静态方法是被调用的时候才执行的。**

**静态的方法可以被继承，但是不能重写。**因为静态方法从程序开始运行后就已经分配了内存，也就是说**已经写死了。**所有引用到该方法的对象（父类的对象也好子类的对象也好）所**指向的都是同一块内存中的数据**，也就是该静态方法。子类中如果定义了相同名称的静态方法，并不会重写，而应该是在内存中又分配了一块给子类的静态方法，没有重写这一说。





### 内部类

内部类分为静态内部类和非静态内部类，非静态内部类又可以分为局部类、匿名类、普通（成员）内部类。

非静态内部类的创建依赖于外围类，拥有一个隐式地指向外部类的引用，因此可以访问外围对象的所有成员，除了基本数据类型和 String 类型的 static final 变量外，不能存在 static 成员（包括变量、方法、内部类）

静态内部类的创建不依赖于外围类，**不能访问外围对象的非静态成员**，可以拥有 static 成员

**内部类的作用：**

1. **解决了多重继承的问题**：可以让多个内部类以不同的方式实现同一个接口，或继承同一个类
2. **成员内部类能够提供更好的封装**，除了该外围类，其它类都不能访问
3. 内部类可以有多个实例，每个实例都有自己的状态信息，并且与其外围类对象地信息相互独立



#### 静态内部类

静态内部类与非静态内部类之间存在一个最大的区别，我们知道非静态内部类在编译完成之后会隐含地保存着一个引用，该引用是指向创建它的外围类，但是静态内部类却没有。没有这个引用就意味着：

1. 它的创建是不需要依赖外围类的创建。
2. 它不能使用任何外围类的非static成员变量和方法。

**举例：静态内部类实现单例模式**

```java
public class Singleton {
    
    //声明为 private 避免调用默认构造方法创建对象
    private Singleton() {
    }
    
    //声明为 private 表明静态内部该类只能在该 Singleton 类中被访问
    private static class SingletonHolder {
        private static final Singleton INSTANCE = new Singleton();
    }

    public static Singleton getUniqueInstance() {
        return SingletonHolder.INSTANCE;
    }
}
```

当 Singleton 类加载时，静态内部类 SingletonHolder 没有被加载进内存。只有当调用 getUniqueInstance()方法从而触发 SingletonHolder.INSTANCE 时 SingletonHolder 才会被加载，此时初始化 INSTANCE 实例，并且 JVM 能确保 INSTANCE 只被实例化一次。

这种方式不仅具有**延迟初始化**的好处，而且由 JVM 提供了对**线程安全**的支持。



#### 匿名内部类

对于有继承和实现接口，可以使用匿名内部类，简化代码的书写。比如使用Thread的时候，不需要写一个Runnable的实现类，然后再把类传进去。直接`new Thread(){run(){}}`就可以。lambda表达式也是用的匿名内部类，没有创建具体的类

##### 为什么匿名内部类中使用局部变量要用 final 修饰

https://blog.csdn.net/tianjindong0804/article/details/81710268

**匿名内部类是拷贝局部变量的值进入内部类，而不是引用，如果局部变量发生修改，内部类不可感知，为了安全，干脆只允许传入final的。**

这是由 Java 的闭包实现机制决定的，闭包可以简单地认为是：

一个依赖于外部环境自由变量的函数，这个函数能够访问外部环境里的自由变量

Java 到处都是闭包，比如类的成员方法、内部类等，都是闭包。但 Java 编译器对闭包的支持不完整，它会偷偷地把外部局部变量**复制一个副本到闭包里面**，即 Java 编译器实现的只是 **capture-by-value**，并没有实现 capture-by-reference，而只有后者才能保持匿名内部类和外部环境局部变量保持同步。既然**内外不能同步**，Java 就干脆一刀切，不允许改变外部的局部变量。



## this

this关键字用于引用类的当前实例。 例如：

```java
class Manager {
    Employees[] employees;
     
    void manageEmployees() {
        int totalEmp = this.employees.length;
        System.out.println("Total employees: " + totalEmp);
        this.report();
    }
     
    void report() { }
}
```

在上面的示例中，this关键字用于两个地方：

- this.employees.length：访问类Manager的当前实例的变量。
- this.report（）：调用类Manager的当前实例的方法。

此关键字是可选的，这意味着如果上面的示例在不使用此关键字的情况下表现相同。 但是，使用此关键字可能会使代码**更易读或易懂。**



## super

super关键字用于从子类访问父类的变量和方法。 例如：

```java
public class Super {
    protected int number;
     
    protected showNumber() {
        System.out.println("number = " + number);
    }
}
 
public class Sub extends Super {
    void bar() {
        super.number = 10;
        super.showNumber();
    }
}
```

在上面的例子中，Sub 类访问父类成员变量 number 并调用其其父类 Super 的 showNumber（） 方法。

*使用 this 和 super 要注意的问题：*

*在构造器中使用 super() 调用父类中的其他构造方法时，该语句必须处于构造器的首行，否则编译器会报错。另外，this 调用本类中的其他构造方法时，也要放在首行。*

**this、super不能用在static方法中。**

简单解释一下：

被 static 修饰的成员属于类，不属于单个这个类的某个对象，被类中所有对象共享。而 this 代表对本类对象的引用，指向本类对象；而 super 代表对父类对象的引用，指向父类对象；所以， **this和super是属于对象范畴的东西，而静态方法是属于类范畴的东西。**