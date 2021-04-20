==**[京东数科二面：常见的 IO 模型有哪些？Java 中的 BIO、NIO、AIO 有啥区别？](https://www.cnblogs.com/javaguide/p/io.html)**==



# I/O模型

Unix 有五种 I/O 模型：

- 阻塞式 I/O
- 非阻塞式 I/O
- I/O 复用（select 和 poll）
- 信号驱动式 I/O（SIGIO）
- 异步 I/O（AIO）

### 五大 I/O 模型比较

一个输入操作通常包括两个阶段：

- 等待数据准备好
- 从内核向进程复制数据

对于一个套接字上的输入操作，第一步通常涉及等待数据从网络中到达。当所等待数据到达时，它被复制到内核中的某个缓冲区。第二步就是把数据从内核缓冲区复制到应用进程缓冲区。

- 同步 I/O：将数据从内核缓冲区复制到应用进程缓冲区的阶段（第二阶段），应用进程会阻塞。
- 异步 I/O：第二阶段应用进程不会阻塞。

同步 I/O 包括阻塞式 I/O、非阻塞式 I/O、I/O 复用和信号驱动 I/O ，它们的主要区别在第一个阶段。

非阻塞式 I/O 、信号驱动 I/O 和异步 I/O 在第一阶段不会阻塞。

![img](images/socket/five-io-patterns.png)



对各种模型讲个故事，描述下区别：

故事情节为：老李去买火车票，三天后买到一张退票。参演人员（老李，黄牛，售票员，快递员），往返车站耗费1小时。

1.**阻塞I/O模型**

老李去火车站买票，排队三天买到一张退票。

耗费：在车站吃喝拉撒睡 3天，其他事一件没干。

2.**非阻塞I/O模型**

老李去火车站买票，隔12小时去火车站问有没有退票，三天后买到一张票。

耗费：往返车站6次，路上6小时，其他时间做了好多事。

3.**I/O复用模型**

多个人复用一个黄牛（一个黄牛为多个人服务）

[1.select/poll](https://link.zhihu.com/?target=http%3A//1.select/poll)

老李去火车站买票，委托黄牛，然后每隔6小时电话黄牛询问，黄牛三天内买到票，然后老李去火车站交钱领票。 

耗费：往返车站2次，路上2小时，黄牛手续费100元，打电话17次

2.epoll

老李去火车站买票，委托黄牛，黄牛买到后即通知老李去领，然后老李去火车站交钱领票。 

耗费：往返车站2次，路上2小时，黄牛手续费100元，无需打电话

4.**信号驱动I/O模型**

老李去火车站买票，给售票员留下电话，有票后，售票员电话通知老李，然后老李去火车站交钱领票。  

耗费：往返车站2次，路上2小时，免黄牛费100元，无需打电话

5.**异步I/O模型**

老李去火车站买票，给售票员留下电话，有票后，售票员电话通知老李并快递送票上门。 

耗费：往返车站1次，路上1小时，免黄牛费100元，无需打电话

1同2的区别是：自己轮询

2同3的区别是：委托黄牛

3同4的区别是：电话代替黄牛

4同5的区别是：电话通知是自取还是送票上门



## 阻塞式 I/O

**应用进程被阻塞，直到数据从内核缓冲区复制到应用进程缓冲区中才返回。**

应该注意到，在阻塞的过程中，其它应用进程还可以执行，因此阻塞不意味着整个操作系统都被阻塞。因为其它应用进程还可以执行，所以不消耗 CPU 时间，这种模型的 CPU 利用率会比较高。

下图中，recvfrom() 用于接收 Socket 传来的数据，并复制到应用进程的缓冲区 buf 中。这里把 recvfrom() 当成**系统调用。**

![img](images/socket/block-io.png)

**java bio模型：阻塞式的进程模型**

java client端的socket write所有字节流iput到字节流的tcpip缓冲区，java client才能返回。如果网络传输很慢，tcp ip缓冲区塞满，java client必须等待缓冲区有空间，写完才可能返回。



## 非阻塞式 I/O

应用进程执行系统调用之后，内核返回一个错误码。应用进程可以继续执行，但是需要不断的执行系统调用来获知 I/O 是否完成，这种方式称为轮询（polling）。

由于 CPU 要处理更多的系统调用，因此这种模型的 CPU 利用率比较低。

![img](images/socket/non-blocking.png)

## I/O 复用

**I/O multiplexing 这里面的 multiplexing 指的其实是在单个线程通过记录跟踪每一个Sock(I/O流)的状态(对应空管塔里面的Fight progress strip槽)来同时管理多个I/O流**。发明它的原因，是尽量多的提高服务器的吞吐能力。

java的nio就借用了epoll的思想，虽然jdk只支持select模型，但是在linux2.6的内核以上，jdk的源码是直接用的epoll的模型。著名的NIO框架netty，就是基于epoll模型完成多路复用机制。

使用 select 或者 poll 等待数据，并且可以等待多个套接字中的任何一个变为可读。这一过程会被阻塞，当某一个套接字可读时返回，之后再使用 recvfrom 把数据从内核复制到进程中。

这样，整个过程只在调用select、poll、epoll这些调用的时候才会阻塞，收发客户消息是不会阻塞的，整个进程或者线程就被充分利用起来，**它可以让单个进程具有处理多个 I/O 事件的能力。又被称为 Event Driven I/O，即事件驱动 I/O，或者 所谓的reactor模式。**

如果一个 Web 服务器没有 I/O 复用，那么每一个 Socket 连接都需要创建一个线程去处理。如果同时有几万个连接，那么就需要创建相同数量的线程。相比于多进程和多线程技术，**I/O 复用不需要进程线程创建和切换的开销，系统开销更小。**



![img](images/socket/multiplexing.png)

## 信号驱动 I/O

应用进程使用 sigaction 系统调用，内核立即返回，应用进程可以继续执行，也就是说等待数据阶段应用进程是非阻塞的。内核在数据到达时向应用进程发送 SIGIO 信号，应用进程收到之后在信号处理程序中调用 recvfrom 将数据从内核复制到应用进程中。

相比于非阻塞式 I/O 的轮询方式，信号驱动 I/O 的 CPU 利用率更高。

![img](images/socket/signal.png)

## 异步 I/O

应用进程执行 aio_read 系统调用会立即返回，应用进程可以继续执行，不会被阻塞，内核会在所有操作完成之后向应用进程发送信号。

异步 I/O 与信号驱动 I/O 的区别在于，异步 I/O 的信号是通知应用进程 I/O 完成，而信号驱动 I/O 的信号是通知应用进程可以开始 I/O。

![img](images/socket/asynchronous-io.png)

# I/O多路复用

非常好的回答：[IO 多路复用是什么意思？ - 罗志宇的回答 - 知乎](https://www.zhihu.com/question/32163005/answer/55772739) 

**多路网络连接复用一个io线程。I/O multiplexing 这里面的 multiplexing 指的其实是在单个线程通过记录跟踪每一个Sock(I/O流)的状态(对应空管塔里面的Fight progress strip槽)来同时管理多个I/O流**。

**select, poll, epoll 都是I/O多路复用的具体的实现，之所以有这三个鬼存在，其实是他们出现是有先后顺序的。**

I/O多路复用这个概念被提出来以后， select是第一个实现 (1983 左右在BSD里面实现的)。

select 被实现以后，很快就暴露出了很多问题。 

- select 会修改传入的参数数组，这个对于一个需要调用很多次的函数，是非常不友好的。
-  select 如果任何一个sock(I/O stream)出现了数据，select 仅仅会返回，但是并**不会告诉你是那个sock上有数据，于是你只能自己一个一个的找**，10几个sock可能还好，要是几万的sock每次都找一遍，这个无谓的开销就颇有海天盛筵的豪气了。
- 一个select 只能监视1024个链接， 这个跟草榴没啥关系哦，linux 定义在头文件中的，参见*FD_SETSIZE。*
- select 不是线程安全的，如果你把一个sock加入到select, 然后突然另外一个线程发现，尼玛，这个sock不用，要收回。对不起，这个select 不支持的，如果你丧心病狂的竟然关掉这个sock, select的标准行为是。。呃。。不可预测的， 这个可是写在文档中的哦。

“If a file descriptor being monitored by select() is closed in another thread, the result is unspecified”
霸不霸气



**linux select多路复用模型：变更触发轮询查找 有1024的数量上限**

while循环，先阻塞，监听1024个客户端是否有变化，若有变化则唤醒自己。然后for循环遍历1024个连接，找到发生变化的一个或多个，执行read操作

好处：一旦被唤醒，就代表有数据可以读或者写了。

缺点：

1. for循环要遍历一遍看哪个有变化，1024个只有一个变化了，遍历很费时间。
2. 有上限，理论上只能复用1024个。



于是14年以后(1997年）一帮人又实现了poll,  poll 修复了select的很多问题，比如 

- poll 去掉了1024个链接的限制，于是要多少链接呢， 主人你开心就好。
- poll 从设计上来说，不再修改传入数组，不过这个要看你的平台了，所以行走江湖，还是小心为妙。

其实拖14年那么久也不是效率问题， 而是那个时代的硬件实在太弱，一台服务器处理1千多个链接简直就是神一样的存在了，select很长段时间已经满足需求。

但是poll仍然不是线程安全的， 这就意味着，不管服务器有多强悍，你也只能在一个线程里面处理一组I/O流。你当然可以那多进程来配合了，不过然后你就有了多进程的各种问题。

于是5年以后, 在2002, 大神 Davide Libenzi 实现了epoll.

epoll 可以说是I/O 多路复用最新的一个实现，epoll 修复了poll 和select绝大部分问题, 比如： 

- epoll 现在是线程安全的。 
- epoll 现在不仅告诉你sock组里面数据，还会告诉你具体哪个sock有数据，变更触发回调直接读取，你不用自己去找了。 
- 而且内核态与用户态共享epfd，不需要内核态和用户态的切换



先阻塞，监听100个客户端是否有变化，若有变化则唤醒自己并执行。epoll模型中有变化的数据会被置为重排文件描述符fd（排到最前面），并且有返回值为重排的个数，只需要执行read或write有变化的。

read和write只是用户态和内核态之间的拷贝，很快。

优点：

1. 不需要遍历，直接执行有变化的。且没有上限
2. 而且内核态与用户态共享epfd，不需要内核态和用户态的切换
