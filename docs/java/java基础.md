

# Java 面向对象

### 谈谈对面向过程编程、面向对象编程还有面向切面编程的理解？

面向过程编程：将问题划分为多个步骤，一步一步实现以解决问题。

面向对象编程： 把一个问题分解成多个对象，然后使用这些对象互相配合以解决问题。

面向切面编程：通过预编译方式和运行期**动态代理**实现程序功能的统一维护的一种技术，可以在运行时（或者编译期、类加载时）动态地将代码切入到类的指定方法的指定位置上。



### 封装

封装把一个对象的属性私有化，同时提供一些可以被外界访问的属性的方法，如果属性不想被外界访问，我们大可不必提供方法给外界访问。但是如果一个类没有提供给外界访问的方法，那么这个类也没有什么意义了。

### 继承

继承是使用已存在的类的定义作为基础建立新类的技术，**新类的定义可以增加新的数据或新的功能，也可以用父类的功能，但不能选择性地继承父类，**(注意关联设计原则中的里氏替换原则)。通过使用继承我们能够非常方便地复用以前的代码。

关于继承如下 3 点请记住：

1. 子类拥有父类对象所有的属性和方法（包括私有属性和私有方法），但是**父类中的私有属性和方法子类是无法访问，只是拥有。**
2. 子类可以拥有自己属性和方法，即子类可以对父类进行扩展。
3. 子类可以用自己的方式实现父类的方法。



### 多态

在 Java 虚拟机中，一切方法调用在 Class 文件里面存储的都只是符号引用，需要在**类加载期间，甚至到运行期间才能确定目标方法的直接引用。**多态之所以能够被实现，是由字节码指令 **invokevirtual** 的特性决定的，**invokevirtual 指令会在运行时找到对象的实际类型，并调用对应的方法。**而出于性能的考虑，大部分虚拟机会为类在方法区中建立一个虚方法表（invokeinterface 对应接口方法表），使用虚方法表来代替元数据查找以提高性能。

**在Java中有两种形式可以实现多态：继承（多个子类对同一方法的重写）和接口（实现接口并覆盖接口中同一方法）。**



### 抽象类和接口的异同

抽象类：含有 abstract 修饰符的 class 就算抽象类；它既可以有抽象方法，也可以有普通方法，构造方法，静态方法，但是不能有抽象构造方法和抽象静态方法。且如果其子类没有实现其所有的抽象方法，那么该子类也必须是抽象类；**抽象类除了不能实例化之外，和普通的类没有区别**

接口：他可以看成是抽象类的一个特例，使用 interface 修饰符；

内部结构：

​    jdk7：接口只有常量和抽象方法，无构造器

​    **jdk8：接口增加了默认方法和静态方法，无构造器**

 (详见issue:https://github.com/Snailclimb/JavaGuide/issues/146)

存疑：jdk9：接口允许以private修饰的方法，无构造器

**共同点：**

​    **不能实例化；**

**不同点：**

- **抽象类是单继承的，而接口可以多继承（实现），**接口自己本身可以通过extends关键字扩展多个接口。
- **从设计层面来说，抽象是对类的抽象，是一种模板设计，而接口是对行为的抽象，是一种行为的规范。**
- **接口中除了static、final变量，不能有其他变量，而抽象类中则不一定。**

接口方法默认修饰符是public，抽象方法可以有public、protected和default这些修饰符，抽象方法就是为了被重写所以不能使用private关键字修饰。



**抽象类能否实例化，理论依据是什么？**

不能，抽象类是不完整的，某些方法可能只有声明，而没有定义（实现），调用这些方法会出现未知的结果。





# 未归类

### java权限控制

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



### Object、泛型、通配符区别

`?`是一个不确定的类型，通常用于**泛型方法的调用代码和形参**，不能用于定义类和泛型方法。用于读取未知类型容器中的元素

`<?>`

？的默认是实现是? extends Object，表示`?`是继承Object的任意类型。

`<? extends T>` **上限通配**

这里?表示一个未知的类，而T是一个具体的类，在实际使用的时候T需要替换成一个具体的类，表示**实例化的时候泛型参数要是T或T的子类。**

`<? super T>` **下限通配**

这里?表示一个未知的类，而T是一个具体的类，在实际使用的时候T需要替换成一个具体的类，表示**实例化的时候泛型参数要是T或是T的父类。**

i. 在Java集合框架中，对于参数值是未知类型的容器类，只能读取其中元素，不能向其中添加元素， 因为，其类型是未知，所以编译器无法识别添加元素的类型和容器的类型是否兼容，唯一的例外是NULL

ii. 而泛型方法中的泛型参数对象是可修改的，因为类型参数T是确定的（在调用方法时确定）



### xxx 与 yyy 的 异同？辨析

#### throw 与 throws

​    throw：手动抛出异常，一般出现在函数体中；

​    throws：声明方法可能抛出的异常，一般出现在**方法头部**；

#### final 、finally 与 finalize

​    final：用于声明属性，方法和类，**表示属性不可变，方法不可重写，类不可继承；**

​    finally：它是异常处理语句结构的一部分，表示总是会执行；

​    finalize：它是Object类的 一个方法，在 垃圾收集器 执行 的时候会调用被回收对象的此方法，可以重写此方法 提供垃圾收集时代的其他资源回收，例如关闭文件等。jvm 不保证此方法总被调用；

#### Collection 与 Collections

​     Collection：它是接口， 集合类的上级接口，继承与他有关的接口主要有List和Set；

​    Collections：它 是针对集合类的一个帮助类，他提供一系列静态方法实现对各种集合的搜索、排序、线程安全等操作；如 Collections.sort（xxx）；

#### ArrayList 与 LinkedList、Vector

​        ArrayList：对数组进行封装，实现长度可变的数组，和数组采用相同的存储方式，在内存中分配连续的空间。优点在于遍历元素和随机访问元素效率高；多线程不安全；

​        LinkedList：采用双向链表的存储方式，优点在于插入和删除元素效率高；多线程不安全；

​        **Vector：类似于 ArrayList；但其使用了 synchronized 方法**（多线程安全），使得性能上比 ArrayList 差；同时，在数组扩容时， ArrayList 是增加原来的 0.5倍，变成 1.5倍长度，而Vector 是增加 1倍，变成 2倍长度。

​        jdk7：创建 ArrayList 对象时，默认长度为 10，类似饿汉模式

​        jdk8：创建 ArrayList 对象时，默认长度为 0，在你第一次插入数据时，创建一个 长度为10 的数组，类似懒汉模式

#### HashMap、LinkedHashMap、Hashtable、ConcurrentHashMap

​        HashMap是基于哈希表的Map接口的非同步实现（多线程不安全）， 允许使用null值和null键(HashMap最多只允许一条记录的键为null，允许多条记录的值为null)；

​        Hashtable也是一个散列表，它存储的内容是键值对。key和value都不允许为null，Hashtable遇到null，直接返回NullPointerException。 线程安全,几乎所有的public的方法都是synchronized的，较HashMap速度慢。

​        ConcurrentHashMap是弱一致性，也就是说遍历过程中其他线程可能对链表结构做了调整，因此get和containsKey返回的可能是过时的数据。ConcurrentHashMap是基于**分段锁**设计来实现线程安全性，只有在同一个分段内才存在竞态关系，不同的分段锁之间没有锁竞争。

​        LinkedHashMap是HashMap的一个子类，它保留插入顺序，帮助我们实现了有序的HashMap。 其维护一个双向链表，并不是说其除了维护存入的数据，另外维护了一个双向链表对象，而是说其根据重写HashMap的实体类Entry，来实现能够将HashMap的数据组成一个双向列表，其存储的结构还是数组+链表的形式。

​        TreeMap 是一个有序的key-value集合，它是通过红黑树实现的。 该映射根据其键的自然顺序(字母排序)进行排序，或者根据创建映射时提供的 Comparator 进行排序，具体取决于使用的构造方法。 TreeMap是非线程安全的。

​        jdk7：创建 HashMap对象时，默认长度为 16，类似饿汉模式（内部数据结构是数组加链表）；新加元素时 元素往头部添加

​        jdk8：创建 HashMap对象时，默认长度为 0，在你第一次插入数据时，创建一个 长度为16的数组，类似懒汉模式（内部数据结构是数组加链表再加红黑树）；新加元素时元素往树尾部添加。

转换为红黑树的条件：一个是链表的长度达到8个，一个是数组的长度达到64个

#### http 、 https

​        HTTP协议传输的数据都是未加密的，也就是明文的，因此使用HTTP协议传输隐私信息非常不安全，为了保证这些隐私数据能加密传输，于是网景公司设计了SSL（Secure Sockets Layer）协议用于对HTTP协议传输的数据进行加密，从而就诞生了HTTPS。简单来说，HTTPS协议是由SSL+HTTP协议构建的可进行加密传输、身份认证的网络协议，要比http协议安全。

​        HTTPS和HTTP的区别主要如下：

​        1、https协议需要到ca申请证书，一般免费证书较少，因而需要一定费用。

​        2、http是超文本传输协议，信息是明文传输，https则是具有安全性的ssl加密传输协议。

​        3、http和https使用的是完全不同的连接方式，用的端口也不一样，前者是80，后者是443。

​        4、http的连接很简单，是无状态的；HTTPS协议是由SSL+HTTP协议构建的可进行加密传输、身份认证的网络协议，比http协议安全。

#### json、gson

​        JSON是一种与开发语言无关的,轻量级的数据格式,全称是JavaScript Object Notation,现在几乎每种语言都有处理JSON的API。

​        Gson是Google提供的用来在Java对象和JSON数据之间进行映射的Java类库,可以将一个JSON字符串转成一个Java对象,或者反过来 。

#### == / equals（）

​        java中的数据类型分为两种:

​        一 、基本数据类型:

​         byte、short、int、long、float、double、char、boolean

​          比较它们需要用  ==  ，比较的是它们的值是否相等

​           二、引用数据类型：

​           也就是对基本数据类型的封装，用 == 比较的是它们的内存地址（其实还是比较的基本数据类型，它们的内存地址不就是int吗）。当new的时候，会给它一个新的内存地址，所以再通过==比较，就会返回false;在Object类中的equals方法其实比较的也是内存地址，用==和equals方法比较结果是一样的，但在一些类中把equals方法重写了，如String、Integer等类中，而不是单纯的比较内存地址了，而是比较内容或者值是否相等。

​            这个equals方法不是固定的，有需要的时候，我们根据情况自己重写。

#### cookie / session

​        cookie数据存放在客户的浏览器上，session数据放在服务器上。

​        cookie不是很安全，别人可以分析存放在本地的COOKIE并进行COOKIE欺骗

​       考虑到安全应当使用session。

​        session会在一定时间内保存在服务器上。当访问增多，会比较占用你服务器的性能

​       考虑到减轻服务器性能方面，应当使用COOKIE。

​        单个cookie保存的数据不能超过4K，很多浏览器都限制一个站点最多保存20个cookie。

​    所以个人建议：

​           将登陆信息等重要信息存放为SESSION

​           其他信息如果需要保留，可以放在COOKIE中

#### DOM / SAX 解析

​        SAX解析方式：逐行扫描文档，一遍扫描一遍解析。相比于DOM，SAX可以在解析文档的任意时刻停止解析解析，是一种速度更快，更高效的方法。 优点：解析可以立即开始，速度快，没有内存压力 缺点：不能对结点做修改 ；适用于读取 XML文件。

​        DOM解析方式：DOM解析器在解析XML文档时，会把文档中的所有元素，按照其出现的层次关系，解析成一个个Node对象（节点） 优点：把XML文件在内存中构建属性结构，可以遍历和修改节点。 缺点：如果文件比较大，内存有压力，解析的时间会比较长。适用于修改 XML文件

#### stack / queue

​        栈与队列的相同点： 

​            1.都是线性结构。 

​            2.插入操作都是限定在表尾进行。

​            3.都可以通过顺序结构和链式结构实现。

​            4.插入与删除的时间复杂度都是O（1），在空间复杂度上两者也一样。

​            5.多链栈和多链队列的管理模式可以相同。 

​        栈与队列的不同点： 

​            1.删除数据元素的位置不同，栈的删除操作在表尾进行，队列的删除操作在表头进行。 

​            2.应用场景不同；常见栈的应用场景包括括号问题的求解，表达式的转换和求值，函数调用和递归实现，深度优先搜索遍历等；常见的队列的应用场景包括计算机系统中各种资源的管理，消息缓冲器的管理和广度优先搜索遍历等。 

​            3.顺序栈能够实现多栈空间共享，而顺序队列不能。



## 逃逸

### this引用逃逸

todo：https://www.cnblogs.com/jian0110/p/9369096.html 

**多线程的this逃逸：构造方法中直接new了一个class的对象。**this逃逸是指当一个对象还没有完成构造（构造方法尚未返回）的时候，其他线程就已经可以获得到该对象的引用，并可以通过该引用操作该对象

解决办法：不要在对象的构造方法中使用this引用逃逸。要将启动线程的动作延迟到构造方法完成之后。

```java
if (uniqueInstance == null) {
   synchronized (Singleton.class) {
       uniqueInstance = new Singleton();
  }
}
```

uniqueInstance 采用 volatile 关键字修饰，防止this逃逸。 uniqueInstance = new Singleton(); 这段代码其实是分为三步执行：

1. **为 uniqueInstance 分配内存空间**
2. **初始化 uniqueInstance**
3. **将 uniqueInstance** **指向分配的内存地址**

但是由于 JVM 具有指令重排的特性，执行顺序有可能变成 1>3>2。指令重排在单线程环境下不会出现问题，但是在**多线程环境下会导致一个线程获得还没有初始化的实例。**例如，线程 T1 执行了 1 和 3，此时 T2 调用 getUniqueInstance() 后发现 uniqueInstance 不为空，因此返回 uniqueInstance，但此时 uniqueInstance 还未被初始化。

使用 volatile 可以禁止 JVM 的指令重排，保证在多线程环境下也能正常运行。





# 常用类

### Object

**Object类中常见的方法**

**Object() ：默认构造方法** 

**getClass() ：返回一个对象的运行时类。 用于反射**

**toString()：**输出一个对象的地址字符串（哈希code码）；可以通过重写toString方法，获取对象的属性！ 

**equals()：**比较的是对象的引用是否指向同一块内存地址， 重写equals()方法比较两个对象的内容是否相同 

**hashCode() ：**返回该对象的哈希码值。

**clone() ：创建并返回此对象的一个副本。** 

**finalize() ：**当垃圾回收器确定不存在对该对象的更多引用时，由对象的垃圾回收器调用此方法。 

**notify() ：**唤醒在此对象监视器上等待的单个线程。 

**notifyAll() :** 唤醒在此对象监视器上等待的所有线程。 

**wait() :** 导致当前的线程等待，直到其他线程调用此对象的 notify() 方法或 notifyAll() 方法。 

**wait(long timeout) :** 导致当前的线程等待，直到其他线程调用此对象的 notify() 方法或 notifyAll() 方法，或者超过指定的时间量。 

**wait(long timeout, int nanos) :** 导致当前的线程等待，直到其他线程调用此对象的 notify() 方法或 notifyAll() 方法，或者其他某个线程中断当前线程，或者已超过某个实际时间量。

**为什么wait notify会放在Object里边？wait(),notify(),notifyAll()用来操作线程为什么定义在Object类中？** 

1、这些方法存在于同步中；

2、使用这些方法必须标识同步所属的锁；

3、**锁可以是任意对象，所以任意对象调用方法一定定义在Object类中。**



## Collection

​        Collection是一个接口，它主要的两个分支是：List 和 Set。

​        List和Set都是接口，它们继承于Collection。List是有序的队列，List中可以有重复的元素；而Set是数学概念中的集合，Set中没有重复元素！

​        List和Set都有它们各自的实现类。

​         为了方便，我们抽象出了AbstractCollection抽象类，它实现了Collection中的绝大部分函数；这样，在Collection的实现类中，我们就可以通过继承AbstractCollection省去重复编码。AbstractList和AbstractSet都继承于AbstractCollection，具体的List实现类继承于AbstractList，而Set的实现类则继承于AbstractSet。

​          另外，Collection中有一个iterator()函数，它的作用是返回一个Iterator接口。通常，我们通过Iterator迭代器来遍历集合。ListIterator是List接口所特有的，在List接口中，通过ListIterator()返回一个ListIterator对象。



## Map

> Map不属于Collection，但也是Java最常用的类之一，也放到这一部分。

[HashMap](https://blog.csdn.net/woshimaxiao1/article/details/83661464)

[HashMap底层实现原理](https://blog.csdn.net/tuke_tuke/article/details/51588156)

### HashMap死循环问题

hashmap线程不安全，在扩容的时候会导致死循环 https://www.jianshu.com/p/1e9cf0ac07f4



### Put过程

![HashMap.put方法](images/java基础/20181105181728652.png)

当两个不同的键对象的hashcode相同时，它们会储存在同一个bucket位置的链表中。键对象的equals()方法用来找到键值对。

put添加的元素Entry就是数组中的元素，每个Map.Entry其实就是一个key-value对，它持有一个指向下一个元素的引用，这就构成了链表。

**创建HashMap对象默认情况下，数组大小为16。**

开始扩容的大小=原来的数组大小*loadFactor   16*0.75=12 超过12个元素，进行扩容

扩容是一个非常消耗性能的操作，所以如果我们已经预知HashMap中元素的个数，那么预设元素的个数能够有效的提高HashMap的性能

扩容后大小是原来的2倍，其中加载因子loadFactor的默认值为0.75，这个参数可以再创建对象时在构造方法中指定。

**转换为红黑树的条件：链表的长度达到8个或数组的长度达到64个**



### 计算插入数组索引

```java
static final int hash(Object key) {
    int h;
    return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);//key如果是null 新hashcode是0 否则 计算新的hashcode
}

//计算数组槽位
(n - 1) & hash
```

首先获取到key的hashcode，与右移16异或，然后对长度进行取模。**return h & (length-1);**  **长度16，和len - 1 ：00001111做且运算**

**为什么要右移16位？**

**用key的hashCode和它本身的右移16位进行XOR运算，相当于二次hash，避免hashcode算法不好产生的不均匀。**

算下标的时候只有hash的低16位参与了运算，所以hashCode得到的int值把高16位和低16位进行了一个异或，等于说计算下标时把hash的高16位也参与进来了，减少了hash碰撞。



**其他哈希解决冲突方法**

**开放地址法（找到下一个为空的）  再hash法    公共溢出区方法（所有冲突的都放到溢出区）**



## 排序实现

**Java排序的实现**

**以前是这样的：**

<img src="images/java基础/clipboard.png" alt="img" style="zoom:67%;" />

基本上排序好了，用归并排序  很乱，用快速排序

看源码好像只有Dual-Pivot Quicksort



## String

**Java8的StringBuffer是不是也用了Synchronized Java8的新特性**

https://www.jianshu.com/p/64519f1b1137

### String、StringBuilder、StringBuffer区别 还要总结精炼

String 类不可变，**内部维护的char[] 数组长度不可变，为final修饰**，String类也是final修饰，不存在扩容。字符串拼接，截取，都会生成一个新的对象。频繁操作字符串效率低下，因为每次都会生成新的对象。

**StringBuilder、StringBuffer的方法都会调用AbstractStringBuilder中的公共方法**，如super.append(...)。只是StringBuffer会在方法上加synchronized关键字，进行同步。

StringBuilder 类内部维护可变长度char[] ， 初始化数组容量为16，存在扩容， **其append拼接字符串方法内部调用System的native方法，进行数组的拷贝，不会重新生成新的StringBuilder对象。**非线程安全的字符串操作类， 其每次调用 toString方法而重新生成的String对象，不会共享StringBuilder对象内部的char[]，会进行一次char[]的copy操作。

StringBuffer 类内部维护可变长度char[]， 基本上与StringBuilder一致，但其为线程安全的字符串操作类，**大部分方法都采用了Synchronized关键字修改**，以此来实现在多线程下的操作字符串的安全性。其toString方法而重新生成的String对象，会共享StringBuffer对象中的toStringCache属性（char[]），但是每次的StringBuffer对象修改，都会置null该属性值。





# 反射

JAVA反射机制是在运行状态中，**对于任意一个类，在JVM第一次读到一种class时，会创建一个对应的Class实例，实例中保存了该class的所有信息——可以知道这个类的所有属性和方法；能通过Class对象，来调用它的任意方法和属性。**这种**动态**获取信息以及动态调用对象方法的功能称为java语言的反射机制。

> 获取构造方法，创建对象-newInstance()，获取修改字段-field.set，调用方法method.invoke，无论是否私有。

**注意：**

- **调用私有方法/修改私有属性/调用私有属性/获取私有constructor：setAccessible(true)**
- 除了int等基本类型外，Java的其他类型全部都是class。**class是由JVM在执行过程中动态加载的，**JVM在第一次读取到一种class类型时，将其加载进内存。——类加载
- **每加载一种class，JVM就为其创建一个Class类型的实例，并关联起来。**JVM为每个加载的class及interface创建了对应的Class实例来保存class及interface的所有信息；一旦类被加载到JVM中，同一个类将不会被再次载入。被载入JVM的类都有一个**唯一标识就是该类的全名+类加载器名，即包括包名和类名。**
- **JVM总是动态加载class，可以在运行期根据条件来控制加载class。**



以String类为例，当JVM加载String类时，它首先读取String.class文件到内存，然后，为String类创建一个Class实例并关联起来：Class cls = new Class(String);

这个Class实例是JVM内部创建的，Class类的构造方法是private，只有JVM能创建Class实例，我们自己的Java程序是无法创建Class实例的。

所以，JVM持有的每个Class实例都指向一个数据类型（class或interface）。实例中保存了该class的所有信息，包括类名、包名、父类、实现的接口、所有构造方法、方法、字段等，因此，如果获取了某个Class实例，我们就可以通过这个Class实例获取到该实例对应的class的所有信息，可以通过Class实例创建对象，调用方法，修改字段。



**如果使用反射可以获取并修改private字段的值，那么类的封装还有什么意义？**

正常情况下，我们总是通过p.name来访问Person的name字段，编译器会根据public、protected和private决定是否允许访问字段，这样就达到了数据封装的目的。

**而反射是一种非常规的用法**，使用反射，首先代码非常繁琐，其次，它**更多地是给工具或者底层框架来使用，目的是在不知道目标实例任何信息的情况下，获取特定字段的值。**



**反射机制的相关类**

| 类名          | 用途                                             |
| ------------- | ------------------------------------------------ |
| Class类       | 代表类的实体，在运行的Java应用程序中表示类和接口 |
| Field类       | 代表类的成员变量（成员变量也称为类的属性）       |
| Method类      | 代表类的方法                                     |
| Constructor类 | 代表类的构造方法                                 |



**反射举例**

```java
package com.interview.javabasic.reflect;

public class Robot {
    private String name;
    public void sayHi(String helloSentence){
        System.out.println(helloSentence + " " + name);
    }
    private String throwHello(String tag){
        return "Hello " + tag;
    }
}
```

通过反射获取Robot类中的私有属性，私有方法：

```java
package com.interview.javabasic.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectSample {
    public static void main(String[] args) throws ClassNotFoundException, 
    IllegalAccessException, InstantiationException, InvocationTargetException, 
    NoSuchMethodException, NoSuchFieldException {
         //全名
        Class rc = Class.forName("com.interview.javabasic.reflect.Robot");
        //根据robot的Class对象，创建robot实例，需要强制转换
        Robot r = (Robot) rc.newInstance();
        //Java9 之后上面的创建方法废弃了：
        //Robot r = (Robot) rc.getDeclaredConstructor().newInstance();
        System.out.println("Class name is " + rc.getName());
        //获取方法对象，参数是方法名，方法参数列表对象（方法名+参数列表确定方法）
        Method getHello = rc.getDeclaredMethod("throwHello", String.class);
        //设置这个方法可见
        getHello.setAccessible(true);
        //调用方法 第一个参数是调用方法的对象，后面是参数对象
        Object str = getHello.invoke(r, "Bob");
        System.out.println("getHello result is " + str);
        Method sayHi = rc.getMethod("sayHi", String.class);
        sayHi.invoke(r, "Welcome");
        //获取字段
        Field name = rc.getDeclaredField("name");
        name.setAccessible(true);
        //修改指定对象的这个字段
        name.set(r, "Alice");
        sayHi.invoke(r, "Welcome");
    }
}
```



### Class

**三种获取Class对象的方法**

- **通过静态变量获取**

- **通过实例变量获取**

- **通过完整类名(包名+类名)获取**

```java
Class cls = String.class;
String s = "Hello";    Class cls = s.getClass();
Class cls = Class.forName("java.lang.String");
```

**Class实例在JVM中是唯一的，因此这三种方法获取到的Class都是相同的。**

```java
返回与给定字符串名称的类或接口相关联的类对象。 
public static Class<?> forName(String className) 
    throws ClassNotFoundException

创建实例：返回类型是泛型，需要强制转换  比如：Robot r = (Robot) rc.newInstance();
public T newInstance() throws ...

当我们判断一个实例是否是某个类型时，使用instanceof操作符，返回boolean
Java9 之后上面的创建方法废弃了，推荐使用：
clazz.getDeclaredConstructor().newInstance();


返回类的类加载器：            
public ClassLoader getClassLoader()  

返回由类对象表示的实体的名称（类，接口，数组类，原始类型或void），作为String 。即，完整类名：
public String getName()                                            

获取继承关系：
public Class<? super T> getSuperclass()
public Class<?>[] getInterfaces()

```

```java
获取构造方法，字段，方法  
可以获取当前+父类的public的
getConstructor getMethod  getField  
    
中间+Declared，包括当前类private的  
getDeclaredConstructor(Class<?>... parameterTypes);
    
getDeclaredField(String name);
    
getDeclaredMethod(String name, Class<?>... parameterTypes);
    
+s获取所有  例：Method[] getMethods()   Method[] getDeclaedMethods()
```



getDeclaredMethod之后，需要先设置私有方法的访问检查：

[Field](https://docs.oracle.com/javase/8/docs/api/java/lang/reflect/Field.html), [Method](https://docs.oracle.com/javase/8/docs/api/java/lang/reflect/Method.html), [Constructor](https://docs.oracle.com/javase/8/docs/api/java/lang/reflect/Constructor.html)通用：extends java.lang.reflect.AccessibleObject

<img src="images/java基础/method.png" alt="img" style="zoom:67%;" />

设置私有字段、方法、构造方法。 true的值表示反射对象应该在使用时抑制Java语言访问检查。

```java
public void setAccessible(boolean flag) throws SecurityException
```



### Field

**java.lang.reflect.Field;**

**封装了字段的所有信息**

```java
获取字段的名称，类型，修饰符
getName()：返回字段名称
getType()：返回字段类型 
getModifiers()：返回字段的修饰符，它是一个int，不同的bit表示不同的含义。


获取和修改实例的特定字段值，包括private的
获取指定对象的指定字段的值
private字段要先设置：fieldTag.setAccessible(true);  有可能被JVM阻止
public Object get(Object obj) throws ...

修改某个对象的这个field的值
public void set(Object obj, Object value) throws ...
```



### Method 

返回方法名，字段，返回类型，参数类型

```java
调用方法，第一个参数是调用这个方法的对象，后面的参数是方法的参数对象。
public Object invoke(Object obj, Object... args) throws ...
```

通过反射调用方法时，仍然遵循多态原则。



### Constructor

```java
public T newInstance(Object... initargs) throws ...
public T newInstance(Object... parameters);
```



**反射为何耗性能？ todo**

运行时注解的信息可以在运行时通过反射机制获取

由于反射涉及动态地解析类型，**无法执行 Java 虚拟机的某些优化措施**（比如 JIT？公共子表达式消除？数组范围检查消除？方法内联？逃逸分析？），因此性能低于非反射操作。如果是依赖注入，生成新的类时，还需要执行一遍类的加载过程（加载、验证、准备、解析、初始化）。



# I/O