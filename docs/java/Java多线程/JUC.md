# 并发容器

JDK 提供的这些容器大部分在 **java.util.concurrent** 包中。

- **ConcurrentHashMap:** 线程安全的 HashMap。在进行读操作时(几乎)不需要加锁，而在写操作只对所操作的结点加锁而不影响客户端对其它结点的访问。
- **CopyOnWriteArrayList/Set:** 线程安全的 ArrayList/set，**读取是完全不用加锁的**，**写入也不会阻塞读取操作。**只有写入和写入之间需要进行同步等待。 在读多写少的场合性能非常好，远远好于 Vector。
- **ConcurrentLinkedQueue:** 线程安全的LinkedList，高效的并发**非阻塞队列。采用CAS volatile**
- **BlockingQueue:** 这是一个接口，JDK 内部通过链表、数组等方式实现了这个接口。表示阻塞队列，**非常适合用于作为数据共享的通道。**ArrayBlockingQueue（有界队列）、LinkedBlockingQueue、PriorityBlockingQueue（无界队列）
- **ConcurrentSkipListMap:** 跳表的实现。这是一个 Map，使用跳表的数据结构进行快速查找。



## ConcurrentHashMap

我们知道 HashMap 不是线程安全的，在并发场景下如果要保证一种可行的方式是使用 `Collections.synchronizedMap()` 方法来包装我们的 HashMap。但这是通过使用一个全局的锁来同步不同线程间的并发访问，因此会带来不可忽视的性能问题。

所以就有了 HashMap 的线程安全版本—— ConcurrentHashMap 的诞生。在 ConcurrentHashMap 中，无论是读操作还是写操作都能保证很高的性能：**在进行读操作时(几乎)不需要加锁，而在写操作时通过锁分段技术只对所操作的段加锁而不影响客户端对其它段的访问。**



相关问题：

**Collections的synchronize方法包装一个线程安全的Map，或者直接用ConcurrentHashMap两者的区别是什么？**

前者直接在put和get方法加了synchronize同步，后者采用了结点锁以及CAS支持更高的并发。



### ConcurrentHashMap 线程安全的具体实现方式

#### JDK1.7 分段ReentrantLock

**在 JDK1.7 的时候，`ConcurrentHashMap`（分段锁）** 对整个桶数组进行了分割分段(`Segment`)，每一把锁只锁容器其中一部分数据，多线程访问容器里不同数据段的数据，就不会存在锁竞争，提高并发访问率。

**`ConcurrentHashMap` 是由 `Segment` 数组结构和 `HashEntry` 数组结构组成**。

**Segment 实现了 `ReentrantLock`,所以 `Segment` 是一种可重入锁，扮演锁的角色。**`HashEntry` 用于存储键值对数据。

```java
static class Segment<K,V> extends ReentrantLock implements Serializable {
}
```

一个 `ConcurrentHashMap` 里包含一个 `Segment` 数组。`Segment` 的结构和 `HashMap` 类似，是一种数组和链表结构，一个 `Segment` 包含一个 `HashEntry` 数组，每个 `HashEntry` 是一个链表结构的元素，每个 `Segment` 守护着一个 `HashEntry` 数组里的元素，当对 `HashEntry` 数组的数据进行修改时，必须首先获得对应的 `Segment` 的锁。

**JDK1.7 的 ConcurrentHashMap：**

![JDK1.7的ConcurrentHashMap](images/JUC/ConcurrentHashMap分段锁.jpg)

<p style="text-align:right;font-size:13px;color:gray">http://www.cnblogs.com/chengxiao/p/6842045.html></p>

#### JDK1.8 synchronized节点锁+CAS

>  不同段没有竞争——>不hash冲突就没有锁竞争 

 **到了 JDK1.8 的时候已经摒弃了 `Segment` 的概念，而是直接用 `Node` 数组+链表+红黑树的数据结构来实现，并发控制使用 `synchronized` 和 CAS 来操作。（JDK1.6 以后 对 `synchronized` 锁做了很多优化）** 整个看起来就像是优化过且线程安全的 `HashMap`，虽然在 JDK1.8 中还能看到 `Segment` 的数据结构，但是已经简化了属性，只是为了兼容旧版本； 

数据结构跟HashMap1.8的结构类似，数组+链表/红黑二叉树。超过8，寻址时间复杂度为从O(N)到O(log(N))

**`synchronized` 只锁定当前链表或红黑二叉树的首节点，这样只要 hash 不冲突，就不会产生并发，效率又提升 N 倍。**

**JDK1.8 的 ConcurrentHashMap：**

![Java8 ConcurrentHashMap 存储结构（图片来自 javadoop）](images/JUC/java8_concurrenthashmap.png)

JDK1.8 的 `ConcurrentHashMap` 不在是 **Segment 数组 + HashEntry 数组 + 链表**，而是 **Node 数组 + 链表 / 红黑树**。不过，Node 只能用于链表的情况，红黑树的情况需要使用 **`TreeNode`**。当冲突链表达到一定长度时，链表会转换成红黑树。



### ConcurrentHashMap和Hashtable的区别

`ConcurrentHashMap` 和 `Hashtable` 的区别主要体现在实现线程安全的方式上不同。

- **底层数据结构：**
  - `ConcurrentHashMap` 
    - JDK1.7  **分段的数组+链表** 
    - JDK1.8，**数组+链表/红黑二叉树。** 与HashMap1.8相同
  - `Hashtable`  **数组+链表**  一直没变
- **实现线程安全的方式（重要）：** 
  -  `ConcurrentHashMap` 
    - JDK1.7 分段锁：Segment 实现了 `ReentrantLock`，所以 `Segment` 是一种可重入锁，扮演锁的角色。
    - JDK1.8 并发控制使用 `synchronized` 和 CAS 来操作，`synchronized` 只锁定当前链表或红黑二叉树的首节点。   不同段没有竞争——>不hash冲突就没有锁竞争 
  -  **`Hashtable`(同一把锁)**：全表锁，使用 `synchronized` 来保证线程安全，效率非常低下。不同线程抢占资源，抢不到的阻塞或轮询

**HashTable:**

![HashTable全表锁](images/JUC/HashTable全表锁.png)

<p style="text-align:right;font-size:13px;color:gray">http://www.cnblogs.com/chengxiao/p/6842045.html></p>

## ConcurrentHashMap源码分析

[ConcurrentHashMap源码分析JDK8-get/put/remove方法](https://www.jianshu.com/p/5bc70d9e5410)

[深入浅出ConcurrentHashMap1.8](https://www.jianshu.com/p/c0642afe03e0)

[ConcurrentHashMap源码解析jdk1.8](https://blog.csdn.net/programmer_at/article/details/79715177)

[阿里面试官：说一下JDK1.7 ConcurrentHashMap的实现原理](https://www.163.com/dy/article/FLIG7R2S0531LF4X.html)

**只分析1.8的：**

```java
static class Node<K,V> implements Map.Entry<K,V> {
    /**
          Node节点的hash值和key的hash值相同
          TreeNode节点的hash值
        **/
    final int hash;
    final K key;
    volatile V val;  //volatile确保了val的内存可见性
    volatile Node<K,V> next;//volatile确保了next的内存可见性
    ...
}

transient volatile Node<K,V>[] table;

//TreeNodes used at the heads of bins. 树开头的结点-root
static final class TreeBin<K,V> extends Node<K,V> {
    TreeNode<K,V> root;
    volatile TreeNode<K,V> first;
    volatile Thread waiter;
    volatile int lockState;
    // values for lockState
}

//Nodes for use in TreeBins
static final class TreeNode<K,V> extends Node<K,V> {
    TreeNode<K,V> parent;  // red-black tree links
    TreeNode<K,V> left;
    TreeNode<K,V> right;
    TreeNode<K,V> prev;    // needed to unlink next upon deletion
    boolean red;
}
```

关于table数组，有3个重要方法：对于`table[]`的读写都用的volatile读写：

```java
//以volatile读的方式读取table数组中的元素
static final <K,V> Node<K,V> tabAt(Node<K,V>[] tab, int i) {
    return (Node<K,V>)U.getObjectVolatile(tab, ((long)i << ASHIFT) + ABASE);
}
//以volatile写的方式，将元素插入table数组
static final <K,V> void setTabAt(Node<K,V>[] tab, int i, Node<K,V> v) {
    U.putObjectVolatile(tab, ((long)i << ASHIFT) + ABASE, v);
}
//以CAS的方式，将元素插入table数组
static final <K,V> boolean casTabAt(Node<K,V>[] tab, int i,
                                    Node<K,V> c, Node<K,V> v) {
    //原子的执行如下逻辑：如果tab[i]==c,则设置tab[i]=v，并返回ture.否则返回false
    return U.compareAndSwapObject(tab, ((long)i << ASHIFT) + ABASE, c, v);
}
```

注：volatile基本类型直接操作，volatile引用类型的变量需要Unsafe类中的`getObjectVolatile`，`putObjectVolatile`。

### get

[ConcurrentHashmap(1.8)get操作——为什么它不需要加锁呢/如何保证读到的数据不是脏数据的呢？](https://blog.csdn.net/xx123698/article/details/106993557/)

**get利用volatile特性，不需要同步控制，实现了无锁读。**

具体：

根据key定位hash桶，通过**tabAt的volatile读**，获取hash桶的头结点。

通过头结点Node的**volatile属性next**，遍历Node链表

找到目标node后，读取Node的**volatile属性val**

```java
public V get(Object key) {
    Node<K,V>[] tab; Node<K,V> e, p; int n, eh; K ek;
    int h = spread(key.hashCode());
    if ((tab = table) != null && (n = tab.length) > 0 &&
        //定位目标hash桶，通过tabAt方法valatile读，读取hash桶的头结点
        (e = tabAt(tab, (n - 1) & h)) != null) {
        //第一个节点就是要找的元素
        if ((eh = e.hash) == h) {
            if ((ek = e.key) == key || (ek != null && key.equals(ek)))
                //e.val也是valatile
                return e.val;
        }
        //特殊节点（红黑树，已经迁移的节点（ForwardingNode)等
        else if (eh < 0)
            return (p = e.find(h, key)) != null ? p.val : null;
        //遍历node链表（e.next也是valitle变量）
        while ((e = e.next) != null) {
            if (e.hash == h &&
                ((ek = e.key) == key || (ek != null && key.equals(ek))))
                return e.val;
        }
    }
    return null;
}
```

### put

put方法是一个大的for循环，put成功才会退出。

**当请求的hash桶为空时，采用for循环+CAS的方式无锁插入。**

**当bucket不为空的时候，才用synchronized锁，插入当前结点到链表/红黑树中。**在锁之前，如果正在扩容，当前线程也帮助扩容。

由于锁的粒度是hash桶，多个put线程只有在请求同一个hash桶时，才会被阻塞。请求不同hash桶的put线程，可以并发执行。

```java
public V put(K key, V value) {
    return putVal(key, value, false);
}

/** Implementation for put and putIfAbsent */
final V putVal(K key, V value, boolean onlyIfAbsent) {
    if (key == null || value == null) throw new NullPointerException();
    int hash = spread(key.hashCode());
    int binCount = 0;
    //for循环+CAS操作
    for (Node<K,V>[] tab = table;;) {
        Node<K,V> f; int n, i, fh;
        if (tab == null || (n = tab.length) == 0)
            tab = initTable();
        //hash桶（tab[i]）为空
        else if ((f = tabAt(tab, i = (n - 1) & hash)) == null) {
            //CAS设置tab[i]，不需要加锁
            if (casTabAt(tab, i, null,
                         new Node<K,V>(hash, key, value, null)))
                //如果设置成功，插入成功退出for循环。否则仍在for循环中，会再次检查bucket的状态
                break;                   // no lock when adding to empty bin
        }
        //hash桶（tab[i]）是fwd节点，表示正在扩容
        else if ((fh = f.hash) == MOVED)
            //帮其扩容
            tab = helpTransfer(tab, f);
        else {
            V oldVal = null;
            //hash桶不为空，对tab[i]中的头结点加锁
            synchronized (f) {
                if (tabAt(tab, i) == f) {
                    //node链表
                    if (fh >= 0) {
                        binCount = 1;

                        for (Node<K,V> e = f;; ++binCount) {
                            K ek;
                            //key-value entry已经存在，更新value
                            if (e.hash == hash &&
                                ((ek = e.key) == key ||
                                 (ek != null && key.equals(ek)))) {
                                oldVal = e.val;
                                if (!onlyIfAbsent)
                                    e.val = value;
                                break;
                            }

                            Node<K,V> pred = e;
                            //是尾节点，则插入
                            if ((e = e.next) == null) {
                                pred.next = new Node<K,V>(hash, key,
                                                          value, null);
                                break;
                            }
                        }
                    }
                    //红黑树
                    else if (f instanceof TreeBin) {
                        Node<K,V> p;
                        binCount = 2;
                        if ((p = ((TreeBin<K,V>)f).putTreeVal(hash, key,
                                                              value)) != null) {
                            oldVal = p.val;
                            if (!onlyIfAbsent)
                                p.val = value;
                        }
                    }
                }
            }
            if (binCount != 0) {
                //tab[i]的链表过长，转成红黑树或者扩容（tab.length过短，优先扩容）
                if (binCount >= TREEIFY_THRESHOLD)
                    treeifyBin(tab, i);
                if (oldVal != null)
                    return oldVal;
                break;
            }
        }
    }
    //size属性+1，如果size属性大于扩容阈值（sizeCtl)则扩容
    addCount(1L, binCount);
    return null;
}
```

1. 根据 key 计算出 hashcode 。
2. 如果数组为空，先对数组进行初始化。
3. 如果bucket插入位置处为空，利用 CAS 尝试写入，失败则自旋保证成功插入。
4. 如果当前位置的 `hashcode == MOVED == -1`,则需要进行扩容。
5. 如果都不满足，则利用 synchronized 锁写入数据。
6. 如果数量大于 `TREEIFY_THRESHOLD` 则要转换为红黑树。

2-6是for循环中的内容 if else结构

### remove

和put方法一样，多个remove线程请求不同的hash桶时，可以并发执行。

如图所示：删除的node节点的next依然指着下一个元素。此时若有一个遍历线程正在遍历这个已经删除的节点，这个遍历线程依然可以通过next属性访问下一个元素。从遍历线程的角度看，他并没有感知到此节点已经删除了，这说明了ConcurrentHashMap提供了弱一致性的迭代器。遍历操作可以参考[ConcurrentHashMap源码分析（JDK8） 遍历操作分析](https://www.jianshu.com/p/3e85ac8f8662)

![img](images/JUC/concurrentHashMap-remove方法.png)

```java
public V remove(Object key) {
    return replaceNode(key, null, null);
}

/**
    参数value:当 value==null 时 ，删除节点 。否则 更新节点的值为value
    参数cv:一个期望值， 当 map[key].value 等于期望值cv  或者 cv==null的时候 ，删除节点，或者更新节点的值
*/
final V replaceNode(Object key, V value, Object cv) {
    int hash = spread(key.hashCode());
    for (Node<K,V>[] tab = table;;) {
        Node<K,V> f; int n, i, fh;
        //table还没有初始化或者key对应的hash桶为空
        if (tab == null || (n = tab.length) == 0 ||
            (f = tabAt(tab, i = (n - 1) & hash)) == null)
            break;
        //正在扩容
        else if ((fh = f.hash) == MOVED)
            tab = helpTransfer(tab, f);
        else {
            V oldVal = null;
            boolean validated = false;
            synchronized (f) {
                //cas获取tab[i],如果此时tab[i]!=f,说明其他线程修改了tab[i]。回到for循环开始处，重新执行
                if (tabAt(tab, i) == f) {
                    //node链表
                    if (fh >= 0) {
                        validated = true;
                        for (Node<K,V> e = f, pred = null;;) {
                            K ek;
                            //找的key对应的node
                            if (e.hash == hash &&
                                ((ek = e.key) == key ||
                                 (ek != null && key.equals(ek)))) {
                                V ev = e.val;
                                //cv参数代表期望值
                                //cv==null:表示直接更新value/删除节点
                                //cv不为空，则只有在key的oldValue等于期望值的时候，才更新value/删除节点

                                //符合更新value或者删除节点的条件
                                if (cv == null || cv == ev ||
                                    (ev != null && cv.equals(ev))) {
                                    oldVal = ev;
                                    //更新value
                                    if (value != null)
                                        e.val = value;
                                    //删除非头节点
                                    else if (pred != null)
                                        pred.next = e.next;
                                    //删除头节点
                                    else
                                        //因为已经获取了头结点锁，所以此时不需要使用casTabAt
                                        setTabAt(tab, i, e.next);
                                }
                                break;
                            }
                            //当前节点不是目标节点，继续遍历下一个节点
                            pred = e;
                            if ((e = e.next) == null)
                                //到达链表尾部，依旧没有找到，跳出循环
                                break;
                        }
                    }
                    //红黑树
                    else if (f instanceof TreeBin) {
                        validated = true;
                        TreeBin<K,V> t = (TreeBin<K,V>)f;
                        TreeNode<K,V> r, p;
                        if ((r = t.root) != null &&
                            (p = r.findTreeNode(hash, key, null)) != null) {
                            V pv = p.val;
                            if (cv == null || cv == pv ||
                                (pv != null && cv.equals(pv))) {
                                oldVal = pv;
                                if (value != null)
                                    p.val = value;
                                else if (t.removeTreeNode(p))
                                    setTabAt(tab, i, untreeify(t.first));
                            }
                        }
                    }
                }
            }
            if (validated) {
                if (oldVal != null) {
                    //如果删除了节点，更新size
                    if (value == null)
                        addCount(-1L, -1);
                    return oldVal;
                }
                break;
            }
        }
    }
    return null;
}
```



### [ConcurrentHashMap能完全替代HashTable吗](https://blog.csdn.net/programmer_at/article/details/79715177#4-concurrenthashmap能完全替代hashtable吗)

HashTable虽然性能上不如ConcurrentHashMap，但并不能完全被取代，两者的迭代器的一致性不同的，**HashTable的迭代器是强一致性的，而ConcurrentHashMap是弱一致的。**ConcurrentHashMap的get，clear，iterator 都是弱一致性的。

下面是大白话的解释：

- Hashtable的任何操作都会把整个表锁住，是阻塞的。好处是总能获取最实时的更新，比如说线程A调用putAll写入大量数据，期间线程B调用get，线程B就会被阻塞，直到线程A完成putAll，因此线程B肯定能获取到线程A写入的完整数据。坏处是所有调用都要排队，效率较低。
- ConcurrentHashMap 是设计为非阻塞的。在更新时会局部锁住某部分数据，但不会把整个表都锁住。同步读取操作则是完全非阻塞的。好处是在保证合理的同步前提下，效率很高。坏处是严格来说读取操作不能保证反映最近的更新。例如**线程A调用putAll写入大量数据，期间线程B调用get，则只能get到目前为止已经顺利插入的部分数据。**

在上面对remove的分析也知道remove操作也是弱一致性的。





# AQS

[深入浅出java同步器AQS](https://www.jianshu.com/p/d8eeb31bee5c)

[java中的Unsafe](https://www.jianshu.com/p/a16d638bc921)