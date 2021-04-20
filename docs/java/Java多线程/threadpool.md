[必须要理清的Java线程池](https://www.jianshu.com/p/50fffbf21b39)

# 线程池基础

在《阿里巴巴 Java 开发手册》“并发处理”这一章节，明确指出线程资源必须通过线程池提供，**不允许在应用中自行显示创建线程。**

## 为什么要使用线程池

> 池化技术相比大家已经屡见不鲜了，线程池、数据库连接池、Http 连接池等等都是对这个思想的应用。池化技术的思想主要是为了减少每次获取资源的消耗，提高对资源的利用率。
>
> “线程池”，顾名思义就是一个线程缓存，线程是稀缺资源，如果被无限制的创建，不仅会消耗系统资源，还会降低系统的稳定性，因此Java中提供线程池对线程进行统一分配、调优和监控。

在web开发中，服务器需要接受并处理请求，所以会为一个请求来分配一个线程来进行处理。如果每次请求都新创建一个线程的话实现起来非常简便，但是存在一个问题：如果并发的请求数量非常多，但每个线程执行的时间很短，这样就会频繁的创建和销毁线程，如此一来会大大降低系统的效率。可能出现服务器在为每个请求创建新线程和销毁线程上花费的时间和消耗的系统资源要比处理实际的用户请求的时间和资源更多。

那么有没有一种办法使执行完一个任务，并不被销毁，而是可以继续执行其他的任务呢？这就是线程池了。

这里借用《Java 并发编程的艺术》提到的来说一下使用线程池的好处：

- **降低资源消耗。**通过重复利用已创建的线程**降低线程创建和销毁造成的消耗。** 
- java是KLT，创建销毁线程需要陷入内核态。
- **提高响应速度。**当任务到达时，任务可以**不需要等到线程创建就能立即执行。**
- **提高线程的可管理性。**线程是稀缺资源，如果无限制的创建，不仅会消耗系统资源，还会降低系统的稳定性，使用线程池可以进行**统一的分配，调优和监控。**



**什么时候使用线程池？**

- 单个任务处理时间比较短
- 需要处理的任务数量很大



## 线程池大小如何确定

> 任务密集：CPU+1  IO密集 CPU核数 * (1 + 平均等待时间/平均工作时间)

如果我们设置的线程池数量太小的话，如果同一时间有大量任务/请求需要处理，可能会导致大量的任务在任务队列中排队等待执行，甚至会出现任务队列满了之后任务无法处理的情况，或者大量任务堆积在任务队列导致OOM。这样很明显是有问题的！ CPU根本没有得到充分利用。

但是，如果我们设置线程数量太大，大量线程可能会同时在争取 CPU 资源，这样会导致大量的上下文切换，从而增加线程的执行时间，影响了整体执行效率。

**上下文切换是时间片到了**

有一个简单并且适用面比较广的公式：

**CPU 密集型任务  CPU核数+1**

这种任务消耗的主要是 CPU 资源，可以将线程数设置为 N（CPU 核心数）+1，比 CPU 核心数多出来的一个线程是为了防止线程偶发的缺页中断，或者其它原因导致的任务暂停而带来的影响。一旦任务暂停，CPU 就会处于空闲状态，而在这种情况下多出来的一个线程就可以充分利用 CPU 的空闲时间。

**I/O 密集型任务 CPU核数 \* (1 + 平均等待时间/平均工作时间)**

或者2*N 

这种任务应用起来，系统会用大部分的时间来处理 I/O 交互，而线程在处理 I/O 的时间段内不会占用 CPU 来处理，这时就可以将 CPU 交出给其它线程使用。因此在 I/O 密集型任务的应用中，我们可以多配置一些线程。



## 如果自己实现线程池会怎么写呢？

### 如何重用线程呢？

**一定是一个死循环，不断获取任务。**

无论操作多复杂，最终都会从线程的run()中出来，因此需要while死循环。进行自旋，一直在run()方法中。

### 线程处理不过来任务怎么办呢？

放在一个队列里。

**选什么样的队列呢？**

阻塞队列：满足队列的FIFO。不管并发有多高，在任意时刻，永远只有一个线程能够进行队列的入队或者出队操作。因此是线程安全的队列。



# Executor框架

Executor 框架不仅包括了线程池的管理，还提供了**线程工厂、队列以及拒绝策略**等，Executor 框架让并发编程变得更加简单。

Executor接口是线程池框架中最基础的部分，定义了一个用于执行Runnable的execute方法。

下图为它的继承与实现：

<img src="images/threadpool/Executor.png" alt="img" style="zoom:50%;" />

如上图所示，包括任务执行机制的核心接口 Executor ，以及继承自 `Executor` 接口的 `ExecutorService` 接口。`ThreadPoolExecutor` 和 `ScheduledThreadPoolExecutor` 这两个关键类实现了 `ExecutorService` 接口。`ScheduledThreadPoolExecutor` 继承了 `ThreadPoolExecutor`。



## Executor

**运行新任务的简单接口，将任务提交和任务执行细节解耦。**

```java
public interface Executor {

    /**
     * Executes the given command at some time in the future.  The command
     * may execute in a new thread, in a pooled thread, or in the calling
     * thread, at the discretion of the {@code Executor} implementation.
     */
    void execute(Runnable command);
}
```

执行任务需要实现的 `Runnable` 接口。`Callable` 和 `Thread` 都实现了Runnable，因此传Callable或者Thread对象也可以。



## ExecutorService

**具备管理执行器和任务生命周期的方法，提交任务机制更完善。**

<img src="images/threadpool/executorService.png" alt="img" style="zoom:60%;" />

**ExecutorService定义了线程池的具体行为**

1. `execute(Runnable command)`：实现自Executor。

2. `submit(Runnable/Callable)`：提交Runnable或Callable任务，并返回代表此任务的Future对象。

3. `shutdown()`：关闭线程池，不再接受新任务。
4. `shutdownNow()`：关闭线程池，停止处理正在处理的和排队的任务。
5. `isShutdown()`：当调用 shutdown() 方法后返回为 true。
6. `isTerminated()`：当调用 shutdown() 方法后，并且所有提交的任务完成后返回为 true。

### execute() vs submit()

- execute()方法用于提交不需要返回值的任务，所以无法判断任务是否被线程池执行成功与否。

- submit()方法用于提交需要返回值的任务，线程池会返回一个 Future 类型的对象。
  - 通过这个 Future 对象可以判断任务是否执行成功，并且可以通过 Future 的 get()方法来获取返回值，get()方法会阻塞当前线程直到任务完成
  - 使用 get(long timeout，TimeUnit unit)方法则会阻塞当前线程一段时间后立即返回，这时候有可能任务没有执行完。
  
  

### 异步计算的结果(Future)

Future 接口以及 Future 接口的实现类 FutureTask 类都可以代表异步计算的结果。

1. 主线程首先要创建实现 Runnable 或者 Callable 接口的任务对象。
2. 把创建完成的实现 Runnable/Callable接口的 对象直接交给 ExecutorService 执行: ExecutorService.execute（Runnable command））或者也可以把 Runnable 对象或Callable 对象提交给 ExecutorService 执行（ExecutorService.submit（Runnable task）或 ExecutorService.submit（Callable <T> task））。
3. 如果执行 ExecutorService.submit（…），ExecutorService 将返回一个实现Future接口的对象。
4. 最后，主线程可以执行 Future.get()方法来等待任务执行完成。主线程也可以执行 Future.cancel（boolean mayInterruptIfRunning）来取消此任务的执行。

由于 FutureTask 实现了 Runnable，在第三步也可以创建 FutureTask，然后直接交给 ExecutorService 执行。



**几个注意的地方：**

future.get()会阻塞主线程，等待执行完成。

[java 多线程 Callable -- 分段处理一个大的list 然后再合并结果](https://www.cnblogs.com/sanmubird/p/7985250.html)



## ScheduledThreadPoolExecutor

**ScheduledThreadPoolExecutor** 

支持future和定期执行任务

![img](images/threadpool/scheduledThreadPoolExecutor.png)

ScheduledThreadPoolExecutor继承了ThreadPoolExecutor类，因此，整体上功能一致，线程池主要负责创建线程（Worker类），线程从阻塞队列中不断获取新的异步任务，直到阻塞队列中已经没有了异步任务为止。

但是相较于ThreadPoolExecutor来说，ScheduledThreadPoolExecutor**具有延时执行任务和可周期性执行任务的特性，**ScheduledThreadPoolExecutor重新设计了任务类ScheduleFutureTask，ScheduleFutureTask重写了run方法使其具有可延时执行和可周期性执行任务的特性。

ScheduledThreadPoolExecutor 使用的任务队列 **DelayedWorkQueue封装了一个PriorityQueue**，PriorityQueue 会对队列中的任务进行排序，执行**所需时间短的放在前面先被执行**(ScheduledFutureTask 的 time 变量小的先执行)，如果执行所需时间相同则先提交的任务将被先执行(ScheduledFutureTask 的 squenceNumber 变量小的先执行)。



# ThreadPoolExecutor

==**线程池实现类 ThreadPoolExecutor 是 Executor 框架最核心的类。**==

> 《阿里巴巴 Java 开发手册》中强制线程池不允许使用 Executors 去创建，而是通过 ThreadPoolExecutor 构造函数的方式，这样的处理方式让写的同学更加明确线程池的运行规则，规避资源耗尽的风险
>
> Executors 返回线程池对象的弊端如下：
>
> FixedThreadPool 和 SingleThreadExecutor ： 允许请求的队列长度为 Integer.MAX_VALUE,可能堆积大量的请求，从而导致 OOM。

## 构造方法参数

**构造方法7个参数：**

```java
public ThreadPoolExecutor(
    int corePoolSize,   		//线程池中的核心线程数
    int maximumPoolSize,		//线程池中允许的最大线程数
    long keepAliveTime,			//核心线程外的线程等待的时间超过了keepAliveTime会被回收销毁
    TimeUnit unit, 				//keepAliveTime的单位
    BlockingQueue<Runnable> workQueue,	//用来保存等待被执行的任务的阻塞队列
    ThreadFactory threadFactory,		//创建新线程的工厂，一般用默认的
    RejectedExecutionHandler handler  //阻塞队列满了以后，线程池的饱和策略
)
```

**corePoolSize：线程池中的核心线程数**，当提交一个任务时，线程池创建一个新线程执行任务，直到当前线程数等于corePoolSize；如果当前线程数为corePoolSize，继续提交的任务被保存到阻塞队列中，等待被执行；如果执行了线程池的prestartAllCoreThreads()方法，线程池会提前创建并启动所有核心线程。

**maximumPoolSiz：线程池中允许的最大线程数。**如果当前阻塞队列满了，且继续提交任务，则创建新的线程执行任务，**前提是当前线程数小于maximumPoolSize；**   

**keepAliveTime：**当线程池中的线程数量大于 corePoolSize 的时候，如果这时没有新的任务提交，核心线程外的线程不会立即销毁，而是会等待，直到等待的时间超过了 keepAliveTime才会被回收销毁。

**workQueue**：当新任务来的时候会先判断当前运行的线程数量是否达到核心线程数，如果达到的话，新任务就会被存放在队列中。使用不同的队列有不同的排队机制

**threadFactory**：它是ThreadFactory类型的变量，用来创建新线程。默认使 Executors.defaultThreadFactory() 来创建线程。使用默认的ThreadFactory来创建线程时，会使新创建的线程具有相同的NORM_PRIORITY优先级并且是非守护线程，同时也设置了线程的名称。一般默认即可

**handler**：线程池的饱和策略，当阻塞队列满了，且没有空闲的工作线程，如果继续提交任务，必须采取一种策略处理该任务。

### 有哪些阻塞队列

在JDK中提供了如下阻塞队列：

1、ArrayBlockingQueue：基于数组结构的**有界**阻塞队列，按FIFO排序任务；

2、LinkedBlockingQuene：基于链表结构的阻塞队列，**无界**，按FIFO排序任务，吞吐量通常要高于ArrayBlockingQuene；  FixedThreadPool用的这种

3、**SynchronousQuene**：**一个不存储元素的阻塞队列，每个插入操作必须等到另一个线程调用移除操作，否则插入操作一直处于阻塞状态----所有任务都能被处理**，吞吐量通常要高于LinkedBlockingQuene； 

CachedThreadPool 用的这种

4、priorityBlockingQuene：具有优先级的无界阻塞队列；  **所需时间短的放在前面先被执行**

### 线程池提供的几种拒绝策略

都是ThreadPoolExecutor的内部类。

因此我们也可以根据应用场景实现RejectedExecutionHandler接口，自定义饱和策略，例如记录日志或持久化存储不能处理的任务。

**1、AbortPolicy：直接抛出RejectedExecutionException异常来拒绝新任务的处理，默认策略；**

```java
throw new RejectedExecutionException("Task " + r.toString() + " rejected from " +e.toString());
```

**2、CallerRunsPolicy：用调用者所在的线程来执行任务**。提供可伸缩队列

也就是直接在调用execute方法的线程中运行(run)被拒绝的任务，如果执行程序已关闭，则会丢弃该任务。因此这种策略会降低对于新任务提交速度，影响程序的整体性能。如果您的应用程序可以承受此延迟并且你要求任何一个任务请求都要被执行的话，你可以选择这个策略。

**3、DiscardOldestPolicy：丢弃阻塞队列中靠最前的任务（**最早的未处理的任务请求**），并执行当前任务；**

**4、DiscardPolicy：** 不处理新任务，直接丢弃掉。



### 线程池除了常见的4种拒绝策略，你还知道哪些？

[Todo待总结](https://mp.weixin.qq.com/s?__biz=Mzg2OTA0Njk0OA==&mid=2247485679&idx=1&sn=57dbca8c9ad49e1f3968ecff04a4f735&chksm=cea24724f9d5ce3212292fac291234a760c99c0960b5430d714269efe33554730b5f71208582&token=1141994790&lang=zh_CN#rd)



## execute执行流程

![img](images/threadpool/execute.png)



宰猪场来了很多猪，也就是任务先装核心线程（屠夫），满了再装队列（猪栏），队列满了让非核心线程（临时工）处理。max-core=非核心线程，相当于临时工。再多得话放到拒绝策略。



## 线程池的状态

**线程池的状态都记录在同一个Integer上，切换状态不需要同步几个变量。**

```java
private final AtomicInteger ctl = new AtomicInteger(ctlOf(RUNNING, 0));
private static final int COUNT_BITS = Integer.SIZE - 3;
private static final int CAPACITY   = (1 << COUNT_BITS) - 1;
```

ctl 是对线程池的运行状态和线程池中有效线程的数量进行控制的一个字段， 它包含两部分的信息: 线程池的运行状态 (runState) 和线程池内有效线程的数量 (workerCount)，**高3位保存runState，低29位保存workerCount**。COUNT_BITS 就是29，CAPACITY就是1左移29位减1（29个1），这个常量表示workerCount的上限值，大约是5亿。

**ctl相关方法**

```java
private static int runStateOf(int c)     { return c & ~CAPACITY; }
private static int workerCountOf(int c)  { return c & CAPACITY; }
private static int ctlOf(int rs, int wc) { return rs | wc; }
```

- **runStateOf**：获取运行状态；
- **workerCountOf**：获取活动线程数；
- **ctlOf**：运行状态和活动线程数的值，即传入运行状态和线程数，求ctl。



### 线程池的5种状态

```java
// runState is stored in the high-order bits
private static final int RUNNING    = -1 << COUNT_BITS;
private static final int SHUTDOWN   =  0 << COUNT_BITS;
private static final int STOP       =  1 << COUNT_BITS;
private static final int TIDYING    =  2 << COUNT_BITS;
private static final int TERMINATED =  3 << COUNT_BITS;
```

![img](images/threadpool/state-change.png)

1、RUNNING

线程池处在RUNNING状态时，能够接收新任务，以及对已添加的任务进行处理。 

状态切换：线程池的初始化状态是RUNNING。换句话说，线程池被一旦被创建，就处于RUNNING状态，并且线程池中的任务数为0。

2、 SHUTDOWN

**线程池处在SHUTDOWN状态时，不接收新任务，但能处理已添加的任务。** 

状态切换：调用线程池的shutdown()接口时，线程池由RUNNING -> SHUTDOWN。

3、STOP

状态说明：**线程池处在STOP状态时，不接收新任务，不处理已添加的任务，并且会中断正在处理的任务。**  //法院来查了，全是病猪，正在杀的都扔掉。

状态切换：调用线程池的shutdownNow()接口时，线程池由(RUNNING or SHUTDOWN ) -> STOP。

4、TIDYING

当SHUTDOWN或STOP状态，ctl中记录的工作线程数变为0的时候，会变为TIDYING状态。

当线程池变为TIDYING状态时，会执行钩子函数terminated()。默认的terminated()在ThreadPoolExecutor类中是空的。若用户想在线程池变为TIDYING时，进行相应的处理，可以通过重载terminated()函数来实现。 

5、 TERMINATED

线程池彻底终止，就变成TERMINATED状态。 

状态切换：线程池处在TIDYING状态时，执行完terminated()之后，就会由 TIDYING -> TERMINATED。

进入TERMINATED的条件如下：

- 线程池不是RUNNING状态；
- 线程池状态不是TIDYING状态或TERMINATED状态；
- 如果线程池状态是SHUTDOWN并且workerQueue为空；
- workerCount为0；
- 设置TIDYING状态成功。

### shutdown()和shutdownNow()的区别

shutdown() 关闭线程池，线程池的状态变为 SHUTDOWN。线程池不再接受新任务了，但是队列里的任务得执行完毕。

shutdownNow() 关闭线程池，线程的状态变为 STOP。线程池会终止当前正在运行的任务，并停止处理排队的任务并返回正在等待执行的 List。

### isTerminated() VS isShutdown()

isShutDown 当调用 shutdown() 方法后返回为 true。

isTerminated 当调用 shutdown() 方法后，**并且所有提交的任务完成后返回为 true**



## Executors

**Executor 框架的工具类**

- FixedThreadPool  可重用固定线程数的线程池 
- SingleThreadExecutor  只有一个线程的线程池
- CachedThreadPool  会根据需要创建新线程的线程池

实际上都调用的ThreadPoolExecutor的构造方法，传入参数不同获得不同的效果。

### FixedThreadPool

可重用固定线程数的线程池

```java
public static ExecutorService newFixedThreadPool(int nThreads) {
    return new ThreadPoolExecutor(nThreads, nThreads,
                                  0L, TimeUnit.MILLISECONDS,
                                  new LinkedBlockingQueue<Runnable>());
}
```

corePoolSize和maximumPoolSize都被设置为传入的固定数量参数。阻塞队列使用LinkedBlockingQueue，因此允许请求的队列长度为`Integer.MAX_VALUE`，**可能堆积大量的请求，从而导致 OOM。**

### SingleThreadExecutor

只有一个线程的线程池

```java
public static ExecutorService newSingleThreadExecutor() {
    return new FinalizableDelegatedExecutorService
        (new ThreadPoolExecutor(1, 1,
                                0L, TimeUnit.MILLISECONDS,
                                new LinkedBlockingQueue<Runnable>()));
}
```

corePoolSize 和 maximumPoolSize 都被设置为1，其他参数和 FixedThreadPool 相同，因此同样会导致OOM。

### CachedThreadPool

```java
public static ExecutorService newCachedThreadPool() {
    return new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                                  60L, TimeUnit.SECONDS,
                                  new SynchronousQueue<Runnable>());
}
```

corePoolSize = 0，maximumPoolSize设置为Integer.MAX_VALUE，代表没有核心线程，非核心线程是无界的；keepAliveTime = 60L，空闲线程等待新任务的最长时间是60s；用了阻塞队列**SynchronousQueue，是一个不存储元素的阻塞队列**，每一个插入操作必须等待另一个线程的移除操作，同理一个移除操作也得等待另一个线程的插入操作完成。

执行execute方法时，首先会先执行SynchronousQueue的offer方法提交任务，并查询线程池中是否有空闲线程来执行SynchronousQueue的poll方法来移除任务。**如果有，则配对成功，将任务交给这个空闲线程。否则，配对失败，创建新的线程去处理任务；**当线程池中的线程空闲时，会执行SynchronousQueue的poll方法等待执行SynchronousQueue中新提交的任务。若超过60s依然没有任务提交到SynchronousQueue，这个空闲线程就会终止；因为maximumPoolSize是无界的，所以提交任务的速度 > 线程池中线程处理任务的速度就要不断创建新线程；每次提交任务，都会立即有线程去处理，因此**CachedThreadPool适用于处理大量、耗时少的任务。**

缺点：如果主线程提交任务的速度高于 maximumPool 中线程处理任务的速度时，CachedThreadPool会不断创建新的线程。极端情况下，这样会导致耗尽 cpu 和内存资源。

**无限的最大线程数，可能导致OOM。**



# 源码

### 属性

**工作的线程Worker放在HashSet中：**

```java
private final HashSet<Worker> workers = new HashSet<Worker>();
```

**对worker的修改操作都要加锁。**

```java
 private final ReentrantLock mainLock = new ReentrantLock();
```

**ctl是原子类**：

```java
private final AtomicInteger ctl = new AtomicInteger(ctlOf(RUNNING, 0));
```

对ctl的操作用的原子类中的CAS操作，比如在addWorker中使用到：

```java
/**
 * Attempts to CAS-increment the workerCount field of ctl.
*/
private boolean compareAndIncrementWorkerCount(int expect) {
    return ctl.compareAndSet(expect, expect + 1);
}
```

**几个参数：workQueue是不能变的，其他有Setter的都用volatile修饰。**

```java
private final BlockingQueue<Runnable> workQueue;
private volatile ThreadFactory threadFactory;
private volatile RejectedExecutionHandler handler;
private volatile long keepAliveTime;
private volatile boolean allowCoreThreadTimeOut;
private volatile int corePoolSize;
private volatile int maximumPoolSize;
```



## execute——向线程池提交任务

```java
	public void execute(Runnable command) {
        if (command == null)
            throw new NullPointerException();  //不能是空任务
        /*
         * clt记录着runState和workerCount
         */
        int c = ctl.get();
        /*
         * workerCountOf方法取出低29位的值，表示当前活动的线程数；
         * 如果当前活动线程数小于corePoolSize，则新建一个线程放入线程池中；
         * 并把任务添加到该线程中。
         */
        if (workerCountOf(c) < corePoolSize) {
            /*
             * addWorker中的第二个参数表示限制添加线程的数量是根据corePoolSize来判断还是maximumPoolSize来判断；
             * 如果为true，根据corePoolSize来判断； 这里是用corePoolSize，创建员工(线程)
             * 如果为false，则根据maximumPoolSize来判断
             */
            if (addWorker(command, true))
                return;
            /*
             * 如果添加失败，则重新获取ctl值
             */
            c = ctl.get();
        }
        /*
         * 如果当前线程池是运行状态并且任务添加到队列成功。
         */
        if (isRunning(c) && workQueue.offer(command)) {
            //添加了任务之后，还需要重新判断判断状态，因为有可能被其他线程调用了shutdow或shutdownNow()
            int recheck = ctl.get();  
            // 由于之前已经把command添加到workQueue中了，这时需要移除该command
            // 执行过后通过handler使用拒绝策略对该任务进行处理，整个方法返回
            if (!isRunning(recheck) && remove(command))
                reject(command);
                /*
                 * 获取线程池中的有效线程数，如果数量是0，则执行addWorker方法
                 * 这里传入的参数表示：
                 * 1. 第一个参数为null，表示在线程池中创建一个线程，但不去启动；
                 * 2. 第二个参数为false，将线程池的有限线程数量的上限设置为maximumPoolSize，添加线程时根据maximumPoolSize来判断；
                 * 如果判断workerCount大于0，则直接返回，在workQueue中新增的command会在将来的某个时刻被执行。
                 */
            else if (workerCountOf(recheck) == 0)
                addWorker(null, false);  //创建非核心线程(临时工)
        }
        /*
         * 如果执行到这里，有两种情况：
         * 1. 线程池已经不是RUNNING状态；
         * 2. 线程池是RUNNING状态，但workerCount >= corePoolSize并且workQueue已满。
         * 这时，再次调用addWorker方法，但第二个参数传入为false，将线程池的有限线程数量的上限设置为maximumPoolSize；
         * 如果失败则拒绝该任务
         */
        else if (!addWorker(command, false))
            reject(command);
    }
```

这里要注意一下addWorker(null, false);，也就是创建一个线程，但并没有传入任务，因为任务已经被添加到workQueue中了，所以worker在执行的时候，会直接从workQueue中获取任务。所以，在workerCountOf(recheck) == 0时执行addWorker(null, false);也是为了保证**线程池在RUNNING状态下必须要有一个线程来执行任务。**



## addWorker()

**addWorker方法的主要工作是在线程池中创建一个新的线程并执行**，firstTask参数 用于指定新增的线程执行的第一个任务，core参数为true表示在新增线程时会判断当前活动线程数是否少于corePoolSize，false表示新增线程前需要判断当前活动线程数是否少于maximumPoolSize。

```java
private boolean addWorker(Runnable firstTask, boolean core) {
    retry:  //retry下面的for(;;)都是在判断线程池的生命状态。
    for (;;) {
        int c = ctl.get();
    // 获取运行状态
        int rs = runStateOf(c);
    /*
     * 这个if判断
     * 如果rs >= SHUTDOWN，则表示此时不再接收新任务；
     * 接着判断以下3个条件，只要有1个不满足，则返回false：
     * 1. rs == SHUTDOWN，这时表示关闭状态，不再接受新提交的任务，但却可以继续处理阻塞队列中已保存的任务
     * 2. firsTask为空
     * 3. 阻塞队列不为空
     * 
     * 首先考虑rs == SHUTDOWN的情况
     * 这种情况下不会接受新提交的任务，所以在firstTask不为空的时候会返回false；
     * 然后，如果firstTask为空，并且workQueue也为空，则返回false，
     * 因为队列中已经没有任务了，不需要再添加线程了
     */
     // Check if queue empty only if necessary.
        if (rs >= SHUTDOWN &&
                ! (rs == SHUTDOWN &&
                        firstTask == null &&
                        ! workQueue.isEmpty()))
            return false;
        for (;;) {
            // 获取线程数
            int wc = workerCountOf(c);
            // 如果wc超过CAPACITY，也就是ctl的低29位的最大值（二进制是29个1），返回false；
            // 这里的core是addWorker方法的第二个参数，如果为true表示根据corePoolSize来比较，
            // 如果为false则根据maximumPoolSize来比较。
            // 
            if (wc >= CAPACITY ||
                    wc >= (core ? corePoolSize : maximumPoolSize))
                return false;
            // 尝试增加workerCount，如果成功，则跳出第一个for循环
            if (compareAndIncrementWorkerCount(c))
                break retry;
            // 如果增加workerCount失败，则重新获取ctl的值
            c = ctl.get();  // Re-read ctl
            // 如果当前的运行状态不等于rs，说明状态已被改变，返回第一个for循环继续执行
            if (runStateOf(c) != rs)
                continue retry;
            // else CAS failed due to workerCount change; retry inner loop
        }
    }
    
    boolean workerStarted = false;
    boolean workerAdded = false;
    Worker w = null;
    try {
     // 根据传入的任务：firstTask来创建Worker对象
        w = new Worker(firstTask);
     // 每一个Worker对象都会创建一个线程 下面才是线程的start
        final Thread t = w.thread;  //提取worker的字段--线程
        if (t != null) {    
            final ReentrantLock mainLock = this.mainLock;
            mainLock.lock();   //为什么要加锁？executor可能在多个线程中同时addworker，防止招人招得太多了
            try {
                int rs = runStateOf(ctl.get());
                // rs < SHUTDOWN表示是RUNNING状态；
                // 如果rs是RUNNING状态或者rs是SHUTDOWN状态并且firstTask为null，向线程池中添加线程。
                // 因为在SHUTDOWN时不会在添加新的任务，但还是会执行workQueue中的任务
                if (rs < SHUTDOWN ||
                        (rs == SHUTDOWN && firstTask == null)) {
                    if (t.isAlive()) // precheck that t is startable
                        throw new IllegalThreadStateException();
                    // workers是一个HashSet  记录所有的工人
                    workers.add(w);
                    int s = workers.size();
                    // largestPoolSize记录着线程池中出现过的最大线程数量
                    if (s > largestPoolSize)
                        largestPoolSize = s;
                    workerAdded = true;
                }
            } finally {
                mainLock.unlock();
            }
            if (workerAdded) {
                // 启动线程
                t.start();
                workerStarted = true;
            }
        }
    } finally {
        /**如果workerStarted失败，回滚
        Rolls back the worker thread creation. 
        - removes worker from workers, if present - decrements worker count 
        - rechecks for termination, in case the existence of this worker was holding up termination
        */
        if (! workerStarted)
            addWorkerFailed(w);
    }
    return workerStarted;
}
```



## Worker类

**线程池中的每一个线程被封装成一个Worker对象，ThreadPool维护一个Worker的HashSet。**

```java
private final class Worker
        extends AbstractQueuedSynchronizer
        implements Runnable
    {
        /**
         * This class will never be serialized, but we provide a
         * serialVersionUID to suppress a javac warning.
         */
        private static final long serialVersionUID = 6138294804551838833L;

        /** Thread this worker is running in.  Null if factory fails. */
        final Thread thread;
        /** Initial task to run.  Possibly null. */
        Runnable firstTask;
        /** Per-thread task counter */
        volatile long completedTasks;

        /**
         * 在调用构造方法时，需要把任务传入，这里通过getThreadFactory().newThread(this);
         * 来新建一个线程，newThread方法传入的参数是this，因为Worker本身继承了Runnable接口，
         * 也就是一个线程，所以一个Worker对象在启动的时候会调用Worker类中的run方法。
         */
        Worker(Runnable firstTask) {
            setState(-1); // inhibit interrupts until runWorker
            this.firstTask = firstTask;
            this.thread = getThreadFactory().newThread(this);
        }

        /** Delegates main run loop to outer runWorker  */
        public void run() {
            runWorker(this);
        }

        // Lock methods
        //
        // The value 0 represents the unlocked state.
        // The value 1 represents the locked state.

        protected boolean isHeldExclusively() {
            return getState() != 0;
        }

        protected boolean tryAcquire(int unused) {
            if (compareAndSetState(0, 1)) {
                setExclusiveOwnerThread(Thread.currentThread());
                return true;
            }
            return false;
        }

        protected boolean tryRelease(int unused) {
            setExclusiveOwnerThread(null);
            setState(0);
            return true;
        }

        public void lock()        { acquire(1); }
        public boolean tryLock()  { return tryAcquire(1); }
        public void unlock()      { release(1); }
        public boolean isLocked() { return isHeldExclusively(); }

    	//在shutdownNow时调用，shutdownNow-interruptWorkers方法中调用
        void interruptIfStarted() {
            Thread t;
            if (getState() >= 0 && (t = thread) != null && !t.isInterrupted()) {
                try {
                    t.interrupt();
                } catch (SecurityException ignore) {
                }
            }
        }
    }
```

**Worker类继承了AQS，并实现了Runnable接口（要干活），**注意其中的firstTask和thread属性：firstTask用它来保存传入的任务；**thread是在调用构造方法时通过ThreadFactory来创建的线程，是用来处理任务的线程。**

在调用构造方法时，需要把任务传入，这里通过getThreadFactory().newThread(this);用threadFactory来新建一个线程，newThread方法传入的参数是this，因为Worker本身继承了Runnable接口，也就是一个线程，所以一个**Worker对象在启动的时候会调用Worker类中的run方法。**

### 为什么要继承AQS？

Worker继承了AQS，使用AQS来实现独占锁的功能。为什么不使用ReentrantLock来实现呢？可以看到tryAcquire方法，它是不允许重入的，而ReentrantLock是允许重入的：

1. lock方法一旦获取了独占锁，表示当前线程正在执行任务中；
2. 如果正在执行任务，则不应该中断线程；
3. 如果该线程现在不是独占锁的状态，也就是空闲的状态，说明它没有在处理任务，这时可以对该线程进行中断；
4. 线程池在执行shutdown方法或tryTerminate方法时会调用interruptIdleWorkers方法来中断空闲的线程，interruptIdleWorkers方法会使用tryLock方法来判断线程池中的线程是否是空闲状态；
5. 之所以设置为不可重入，是因为我们不希望任务在调用像setCorePoolSize这样的线程池控制方法时重新获取锁。如果使用ReentrantLock，它是可重入的，这样如果在任务中调用了如setCorePoolSize这类线程池控制的方法，会中断正在运行的线程。

==**所以，Worker继承自AQS，使用不可重入锁，用于判断线程是否空闲以及是否可以被中断。**==

此外，在构造方法中执行了setState(-1);，把state变量设置为-1，为什么这么做呢？是因为AQS中默认的state是0，如果刚创建了一个Worker对象，还没有执行任务时，这时就不应该被中断，看一下tryAquire方法：

```java
protected boolean tryAcquire(int unused) {
//cas修改state，不可重入
    if (compareAndSetState(0, 1)) { 
        setExclusiveOwnerThread(Thread.currentThread());
        return true;
    }
    return false;
}
```

tryAcquire方法是根据state是否是0来判断的，所以，setState(-1);将state设置为-1是为了禁止在执行任务前对线程进行中断。

正因为如此，在runWorker方法中会先调用Worker对象的unlock方法将state设置为0。



### runWorker方法

**runWorker执行任务前为什么要加锁？**

其实上面已经回答了这个问题。**在执行之前要先获取锁，避免在执行任务期间被其他的线程中断。**在`shutdown`中断的时候，调用`interruptIdleWorkers`，只中断没有执行任务的Worker。**执行任务的Worker会获取到锁，`interruptIdleWorkers`中w.tryLock()就会失败。**

```java
/**Interrupts threads that might be waiting for tasks (as indicated by not being locked！！) so they can check for termination or configuration changes. Ignores SecurityExceptions (in which case some threads may remain uninterrupted).**/
private void interruptIdleWorkers(boolean onlyOne) {
    final ReentrantLock mainLock = this.mainLock;
    mainLock.lock();
    try {
        for (Worker w : workers) {
            Thread t = w.thread;
            if (!t.isInterrupted() && w.tryLock()) {
                try {
                    t.interrupt();
                } catch (SecurityException ignore) {
                } finally {
                    w.unlock();
                }
            }
            if (onlyOne)
                break;
        }
    } finally {
        mainLock.unlock();
    }
}
```

**在Worker类中的run方法调用了runWorker方法来执行任务，runWorker方法的代码如下：**

```java
/**
Main worker run loop. Repeatedly gets tasks from queue and executes them, while coping with a number of issues: 
	1. We may start out with an initial task, in which case we don't need to get the first one. Otherwise, as long as pool is running, we get tasks from getTask. If it returns null then the worker exits due to changed pool state or configuration parameters. Other exits result from exception throws in external code, in which case completedAbruptly holds, which usually leads processWorkerExit to replace this thread. 
	2. Before running any task, the lock is acquired to prevent other pool interrupts while the task is executing, and then we ensure that unless pool is stopping, this thread does not have its interrupt set. 
	3. Each task run is preceded by a call to beforeExecute, which might throw an exception, in which case we cause thread to die (breaking loop with completedAbruptly true) without processing the task. 
	4. Assuming beforeExecute completes normally, we run the task, gathering any of its thrown exceptions to send to afterExecute. We separately handle RuntimeException, Error (both of which the specs guarantee that we trap) and arbitrary Throwables. Because we cannot rethrow Throwables within Runnable.run, we wrap them within Errors on the way out (to the thread's UncaughtExceptionHandler). Any thrown exception also conservatively causes thread to die. 
	5. After task.run completes, we call afterExecute, which may also throw an exception, which will also cause thread to die. According to JLS Sec 14.20, this exception is the one that will be in effect even if task.run throws. The net effect of the exception mechanics is that afterExecute and the thread's UncaughtExceptionHandler have as accurate information as we can provide about any problems encountered by user code.
Params:
w – the worker
*/
final void runWorker(Worker w) {
    Thread wt = Thread.currentThread();
    // 获取第一个任务
    Runnable task = w.firstTask;
    w.firstTask = null;
    // 允许中断
    w.unlock(); // allow interrupts  没加锁为什么要解锁？在上面回答了这个问题
    // 是否因为异常退出循环
    boolean completedAbruptly = true;
    try {
        // 如何实现线程复用？如果task为空，则通过getTask来获取任务  
        while (task != null || (task = getTask()) != null) {
            w.lock();
            if ((runStateAtLeast(ctl.get(), STOP) ||
                    (Thread.interrupted() &&
                            runStateAtLeast(ctl.get(), STOP))) &&
                    !wt.isInterrupted())
                wt.interrupt();
            try {
                beforeExecute(wt, task);
                Throwable thrown = null;
                try {
                    task.run();
                } catch (RuntimeException x) {
                    thrown = x; throw x;
                } catch (Error x) {
                    thrown = x; throw x;
                } catch (Throwable x) {
                    thrown = x; throw new Error(x);
                } finally {
                    afterExecute(task, thrown);
                }
            } finally {
                task = null;
                w.completedTasks++;
                w.unlock();
            }
        }
        completedAbruptly = false;
    } finally {
        processWorkerExit(w, completedAbruptly);
    }
}
```

这里说明一下第一个if判断，目的是：

- 如果线程池正在停止，那么要保证当前线程是中断状态；
- 如果不是的话，则要保证当前线程不是中断状态；

这里要考虑在执行该if语句期间可能也执行了shutdownNow方法，shutdownNow方法会把状态设置为STOP，回顾一下STOP状态：

不能接受新任务，也不处理队列中的任务，会中断正在处理任务的线程。在线程池处于 RUNNING 或 SHUTDOWN 状态时，调用 shutdownNow() 方法会使线程池进入到该状态。

STOP状态要中断线程池中的所有线程，而这里使用Thread.interrupted()来判断是否中断是为了确保在RUNNING或者SHUTDOWN状态时线程是非中断状态的，因为Thread.interrupted()方法会复位中断的状态。

**总结一下runWorker方法的执行过程：**

1. **while循环不断地通过getTask()方法获取任务；**
2. **getTask()方法从阻塞队列中取任务；**
3. **如果线程池正在停止，那么要保证当前线程是中断状态，否则要保证当前线程不是中断状态；**
4. **调用task.run()执行任务；**
5. **如果task为null则跳出循环，执行processWorkerExit()方法；**
6. **runWorker方法执行完毕，也代表着Worker中的run方法执行完毕，销毁线程。**

这里的beforeExecute方法和afterExecute方法在ThreadPoolExecutor类中是空的，留给子类来实现。

completedAbruptly变量来表示在执行任务过程中是否出现了异常，在processWorkerExit方法中会对该变量的值进行判断。

### getTask方法

getTask方法用来从阻塞队列中取任务，代码如下：

```java
private Runnable getTask() {
    // timeOut变量的值表示上次从阻塞队列中取任务时是否超时
    boolean timedOut = false; // Did the last poll() time out?
    for (;;) {
        int c = ctl.get();
        int rs = runStateOf(c);
        // Check if queue empty only if necessary.
    /*
     * 如果线程池状态rs >= SHUTDOWN，也就是非RUNNING状态，再进行以下判断：
     * 1. rs >= STOP，线程池是否正在stop；
     * 2. 阻塞队列是否为空。
     * 如果以上条件满足，则将workerCount减1并返回null。
     * 因为如果当前线程池状态的值是SHUTDOWN或以上时，不允许再向阻塞队列中添加任务。
     */
        if (rs >= SHUTDOWN && (rs >= STOP || workQueue.isEmpty())) {
            decrementWorkerCount();
            return null;
        }
        int wc = workerCountOf(c);
        // Are workers subject to culling?
        // timed变量用于判断是否需要进行超时控制。
        // allowCoreThreadTimeOut默认是false，也就是核心线程不允许进行超时；
        // wc > corePoolSize，表示当前线程池中的线程数量大于核心线程数量；
        // 对于超过核心线程数量的这些线程，需要进行超时控制
        boolean timed = allowCoreThreadTimeOut || wc > corePoolSize;

    /*
     * wc > maximumPoolSize的情况是因为可能在此方法执行阶段同时执行了setMaximumPoolSize方法；
     * timed && timedOut 如果为true，表示当前操作需要进行超时控制，并且上次从阻塞队列中获取任务发生了超时
     * 接下来判断，如果有效线程数量大于1，或者阻塞队列是空的，那么尝试将workerCount减1；
     * 如果减1失败，则返回重试。
     * 如果wc == 1时，也就说明当前线程是线程池中唯一的一个线程了。
     */
        if ((wc > maximumPoolSize || (timed && timedOut))
                && (wc > 1 || workQueue.isEmpty())) {
            if (compareAndDecrementWorkerCount(c))
                return null;
            continue;
        }
        try {
        /*
         * 根据timed来判断，如果为true，则通过阻塞队列的poll方法进行超时控制，如果在keepAliveTime时间内没有获取到任务，则返回null；
         * 否则通过take方法，如果这时队列为空，则take方法会阻塞直到队列不为空。
         *
         */
            Runnable r = timed ?
                    workQueue.poll(keepAliveTime, TimeUnit.NANOSECONDS) :
                    workQueue.take();
            if (r != null)
                return r;
            // 如果 r == null，说明已经超时，timedOut设置为true
            timedOut = true;
        } catch (InterruptedException retry) {
            // 如果获取任务时当前线程发生了中断，则设置timedOut为false并返回循环重试
            timedOut = false;
        }
    }
}
```

这里重要的地方是第二个if判断，目的是控制线程池的有效线程数量。由上文中的分析可以知道，在执行execute方法时，如果当前线程池的线程数量超过了corePoolSize且小于maximumPoolSize，并且workQueue已满时，则可以增加工作线程，但这时如果超时没有获取到任务，也就是timedOut为true的情况，说明workQueue已经为空了，也就说明了当前线程池中不需要那么多线程来执行任务了，可以把多于corePoolSize数量的线程销毁掉，保持线程数量在corePoolSize即可。

什么时候会销毁？当然是runWorker方法执行完之后，也就是Worker中的run方法执行完，由JVM自动回收。

getTask方法返回null时，在runWorker方法中会跳出while循环，然后会执行processWorkerExit方法。

### processWorkerExit方法

```java
private void processWorkerExit(Worker w, boolean completedAbruptly) {
    // 如果completedAbruptly值为true，则说明线程执行时出现了异常，需要将workerCount减1；
    // 如果线程执行时没有出现异常，说明在getTask()方法中已经已经对workerCount进行了减1操作，这里就不必再减了。  
    if (completedAbruptly) // If abrupt, then workerCount wasn't adjusted
        decrementWorkerCount();
    final ReentrantLock mainLock = this.mainLock;
    mainLock.lock();
    try {
        //统计完成的任务数
        completedTaskCount += w.completedTasks;
        // 从workers中移除，也就表示着从线程池中移除了一个工作线程
        workers.remove(w);
    } finally {
        mainLock.unlock();
    }
    // 根据线程池状态进行判断是否结束线程池
    tryTerminate();
    int c = ctl.get();
/*
 * 当线程池是RUNNING或SHUTDOWN状态时，如果worker是异常结束，那么会直接addWorker；
 * 如果allowCoreThreadTimeOut=true，并且等待队列有任务，至少保留一个worker；
 * 如果allowCoreThreadTimeOut=false，workerCount不少于corePoolSize。
 */
    if (runStateLessThan(c, STOP)) {
        if (!completedAbruptly) {
            int min = allowCoreThreadTimeOut ? 0 : corePoolSize;
            if (min == 0 && ! workQueue.isEmpty())
                min = 1;
            if (workerCountOf(c) >= min)
                return; // replacement not needed
        }
        addWorker(null, false);
    }
}
```

至此，processWorkerExit执行完之后，工作线程被销毁，以上就是整个工作线程的生命周期，从execute方法开始，Worker使用ThreadFactory创建新的工作线程，runWorker通过getTask获取任务，然后执行任务，如果getTask返回null，进入processWorkerExit方法，整个线程结束，如图所示：

![img](images/threadpool/addWorker.png)