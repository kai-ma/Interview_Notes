## 索引

什么是索引?

索引是一种用于快速查询和检索数据的数据结构。**通常在涉及到文件的管理的应用都会有索引，比如数据库，操作系统文件管理，对象存储。**常见的索引结构有: B树， B+树和Hash。

索引的作用就相当于目录的作用。

**为什么要使用索引/索引的优点**

建立索引的目的是：希望通过索引进行数据查找，减少随机 IO，增加查询性能 ，索引能过滤出越少的数据，则从磁盘中读入的数据也就越少。

**索引是数据库优化的重要方法**    是帮助MySQL高效获取数据的数据结构

1. **可以大大加快数据的检索速度（大大减少的检索的行数——二分查找）, 这也是创建索引的最主要的原因。**
2. **通过创建唯一性索引，可以保证数据库表中每一行数据的唯一性。**
3. **将随机 I/O 变为顺序 I/O**（B+Tree 索引是有序的，会将相邻的数据都存储在一起）。
4. **帮助服务器避免进行排序和分组，以及避免创建临时表**（B+Tree 索引是有序的，可以用于 ORDER BY 和 GROUP BY 操作。临时表主要是在排序和分组过程中创建，不需要排序和分组，也就不需要创建临时表）。
5. 可以加速表和表之间的连接，特别是在实现数据的参考完整性方面特别有意义。





## 存储引擎

**MyISAM的适合场景**

- 频繁执行全表count语句。select count(*) from table 
  - MyISAM用一个变量保存了表的行数。InnoDB需要扫描统计

- 对数据进行增删改的频率不高，查询非常频繁的时候
  - 增删改涉及锁表操作，每次都是表锁，并发性能很差

- 没有事务

**InnoDB适合的场景**

- 数据增删改查都频繁

- 可靠性要求比较高，要求支持事务的系统



### MyISAM和InnoDB的区别



## 事务

> 只有Innodb支持事务

### 事务的四大特性

**1. 原子性（Atomicity）**

事务被视为不可分割的最小单元，**事务的所有操作要么全部提交成功，要么全部失败回滚。**

**回滚可以用回滚日志（Undo Log）来实现，**回滚日志记录着事务所执行的修改操作，在回滚时反向执行这些修改操作即可。

**2. 一致性（Consistency）**

**所有事务对同一个数据的读取结果都是相同的。**

**3. 隔离性（Isolation）**

一个事务所做的修改在最终提交以前，对其它事务是不可见的。

**4. 持久性（Durability）**

一旦事务提交，则其所做的修改将会永远保存到数据库中。即使系统发生崩溃，事务执行的结果也不能丢失。

**系统发生奔溃可以用重做日志（Redo Log）进行恢复**，从而实现持久性。与回滚日志记录数据的逻辑修改不同，重做日志记录的是数据页的物理修改。



### 事务相关SQL

MySQL InnoDB 默认采用自动提交模式。也就是说，如果不显式使用`START TRANSACTION`语句来开始一个事务，那么每个查询操作都会被当做一个事务并自动提交。

```sql
show variables like 'autocommit';

#关闭自动提交 只会关闭当前session的 
set autocommit = 0;
关闭自动提交以后，每次输入SQL语句，再输入commit才会提交并释放锁 

#也可以不修改，显示开启事务
#开启事务 这种操作不需要关闭自动提交事务，显式开启一个事务 下面两种都可以
start transaction; 
begin;

...各种操作

#回滚
rollback;

#提交事务  start transaction之后需要显式commit
commit;

#在事务中创建一个保存点，一个事务中可以有多个 SAVEPOINT
savepoint idenrifier
#删除一个事务的保存点；
RELEASE SAVEPOINT identifier 
#把事务回滚到标记点
ROLLBACK TO identifier 


#查看当前数据库的隔离级别  默认是REPEATABLE-READ
select @@tx_isolation;  

#修改数据库的隔离级别
set session transaction isolation level read uncommitted;
set session transaction isolation level read committed;
set session transaction isolation level repeatable read;
set session transaction isolation level serializable;
```



### 并发事务会带来哪些问题

在典型的应用程序中，多个事务并发运行，经常会操作相同的数据来完成各自的任务（多个用户对同一数据进行操作）。并发虽然是必须的，但可能会导致以下的问题。

#### 丢失修改

一个事务的更新操作被另外一个事务的更新操作替换。一般在现实生活中常会遇到，例如：T1 和 T2 两个事务都对一个数据进行修改，T1 先修改并提交生效，T2 随后修改，T2 的修改覆盖了 T1 的修改。

**类似于多线程对共享变量的修改，因为数据库都会默认给更新加锁，所以这个问题很容易地被解决了。**



#### 脏读

**一个事务读取到了另一个事务未提交的数据。**

例如：T1 修改一个数据但未提交，T2 随后读取这个数据。如果 T1 撤销了这次修改，那么 T2 读取的数据是脏数据。



#### 不可重复读

**多次读取同一数据，结果不一致。**

例如：T2 读取一个数据，T1 对该数据做了修改。如果 T2 再次读取这个数据，此时读取的结果和第一次读取的结果不同。

值得注意的是，读取的时候没关系，会不会影响修改呢？

比如小张账户中的余额是20，事务1读取一遍是20，事务2修改成100，在关闭事务1之前，事务1读取还是20，事务1去做更新操作，加100的话，结果是120不就错了么，应该是200啊。实际上，在用以下SQL语句操作时，事务1中读取的值是20，但加100，数据库中的结果是200（**注意set的时候等号右边也要用balance，而不是用20**）。在事务1commit之前，事务1读取是120，commit之后，事务1读取是200。

`update account_innodb set balance = balance + 100 where name = '小张';`

通过后面的锁原理分析，就知道为什么读取和修改不一样了，读取是读取MVCC多版本快照，修改是修改最新的。



#### 幻读

幻读本质上也属于不可重复读的情况，T1 读取某个范围的若干行，T2 在这个范围内**插入或删除行**，T1 再次读取这个范围的数据，此时读取的结果和和第一次读取的结果不同。



#### 不可重复读和幻读区别

**不可重复读的重点是修改值，幻读的重点在于新增或者删除行**

不可重复读的重点是修改，比如多次读取**同一条记录**发现其中某些列的值被修改。

幻读的重点在于新增或者删除，比如多次读取发现记录增多或减少了。



### 事务的隔离级别

> 产生并发不一致性问题的主要原因是破坏了事务的隔离性，解决方法是通过**并发控制**来保证隔离性。数据库管理系统提供了事务的隔离级别，让用户以一种轻松的方式处理并发一致性问题。

**SQL 标准定义了四个隔离级别：**

- **READ-UNCOMMITTED(读取未提交)：** 最低的隔离级别，允许读取尚未提交的数据变更，**可能会导致脏读、幻读或不可重复读**。
- **READ-COMMITTED(读取已提交)：** 允许读取并发事务已经提交的数据，**可以阻止脏读，但是幻读或不可重复读仍有可能发生**。
- **REPEATABLE-READ(可重复读)：**  **对同一字段的多次读取结果都是一致的**，除非数据是被本身事务自己所修改，**可以阻止脏读和不可重复读，但幻读仍有可能发生**。
- **SERIALIZABLE(可串行化)：** 最高的隔离级别，完全服从ACID的隔离级别。所有的事务依次逐个执行，这样事务之间就完全不可能产生干扰，也就是说，**该级别可以防止脏读、不可重复读以及幻读**。

| 隔离级别         | 脏读 | 不可重复读 | 幻影读 |
| ---------------- | ---- | ---------- | ------ |
| READ-UNCOMMITTED | √    | √          | √      |
| READ-COMMITTED   | ×    | √          | √      |
| REPEATABLE-READ  | ×    | ×          | √      |
| SERIALIZABLE     | ×    | ×          | ×      |

事务隔离级别越高，安全性越高，串行化执行越严重，降低了数据库的并发度，需要根据业务需要去设置默认的隔离级别。Oracle默认为RC，MySQL默认为RR。

MySQL InnoDB 存储引擎的默认支持的隔离级别是 **REPEATABLE-READ（可重复读）**。可以通过`SELECT @@tx_isolation;`命令来查看。

这里需要注意的是：InnoDB 存储引擎在默认 REPEATABLE-READ 事务隔离级别下使用的是Next-Key Lock 锁算法，因此可以避免幻读的产生，已经可以完全保证事务的隔离性要求，**达到了 SQL标准的 SERIALIZABLE(可串行化) 隔离级别。**

Spring默认是RC级别。InnoDB 存储引擎在**分布式事务**的情况下一般会用到 **SERIALIZABLE(可串行化)** 隔离级别。



## MySQL锁

### 锁的分类

- 按锁的粒度划分：行级锁以及表级锁。
- 按锁级别划分：共享锁和排他锁。
- 按加锁方式划分：自动锁和显式锁。
  - 自动锁有：意向锁、MyISAM的表锁、update、insert、delete的时候加的锁
  - 显式锁有：lock ... in share mode、select ... for update

- 按使用方式划分：乐观锁和悲观锁



### 表级锁和行级锁

MySQL 中提供了两种封锁粒度：行级锁以及表级锁。**MyISAM只支持表锁，InnoDB支持行级锁(row-level locking)和表级锁，默认为行级锁。**

- 表级锁： MySQL中锁定 **粒度最大** 的一种锁，对当前操作的整张表加锁，实现简单，资源消耗也比较少，加锁快，不会出现死锁。其锁定粒度最大，触发锁冲突的概率最高，并发度最低，MyISAM和 InnoDB引擎都支持表级锁。
- 行级锁： MySQL中锁定 **粒度最小** 的一种锁，只针对当前操作的行进行加锁。 行级锁能大大减少数据库操作的冲突。其加锁粒度最小，并发度高，但加锁的开销也最大，加锁慢，会出现死锁。

应该尽量只锁定需要修改的那部分数据，而不是所有的资源。锁定的数据量越少，发生锁争用的可能就越小，系统的并发程度就越高。但是加锁需要消耗资源，锁的各种操作（包括获取锁、释放锁、以及检查锁状态）都会增加系统开销，封锁粒度越小，系统开销就越大。因此在选择封锁粒度时，**需要在锁开销和并发程度之间做一个权衡。**



### 共享锁和排他锁

读锁-共享锁，写锁-互斥锁

只有读锁与读锁之间可以兼容，其他两两之间都不兼容。

​	读：select

​	写：update/delete/insert



#### MyISAM锁举例

> **MyISAM引擎用的是表锁，不支持行级锁。**

对于MyISAM表，多个会话同时操作时，**任意操作都会隐式地给整张表上锁**，读操作是共享锁，写操作是排他锁。

```sql
#在这两千条记录被更新完之前，其他会话对这个表的读/写操作都会被阻塞，哪怕是读第3000行。
update table_name_myisam set 字段 = value where id between 1 and 2000;

#普通操作和显式加锁的效果相同，也可以显式加锁。
#因为任何操作都会隐式加锁，作用和直接的select/update/insert/delete操作是一样的
#某个客户端给MyISAM的table加上读锁，其他客户端只能读，写操作会被阻塞，直到锁释放
lock tables table_name_myisam read;

#某个客户端给MyISAM的table加上写锁，其他客户端的所有操作都会被阻塞，直到锁释放
lock tables table_name_myisam write;

#释放锁
unlock tables;
```



**可以对读操作加排他锁**

```sql
select * table_name_myisam where id between 1 and 2000 for update;
```

**这样的读操作加排他锁select for update之后，在执行完之前，对这张表的其他读、写操作都会被阻塞。**



#### InnoDB锁举例

> **InnoDB默认使用行锁，也支持表锁。**

**事务中开启的锁，在事务提交后才会释放。**

**InnoDB默认是非阻塞select，select操作不会影响写操作。**因此下面的两句操作，虽然都是对第三行的并发操作，但不会互相影响，都能执行成功。

```sql
#会话1
start transaction;

#会话2
start transaction;

#会话1
select * from table_name_innodb where id = 3;

#会话2
update table_name_innodb set name = 'java' where id = 3;
```

可以给select操作加上共享锁`lock in share mode`

```sql
#把上面会话1的读操作换成下面的lock in share mode
select * from table_name_innodb where id = 3 lock in share mode;
```

把上面会话1的读操作换成上面的lock in share mode，此时会话2的更新语句被阻塞，**在会话1commit之后，释放读锁，update语句才能加上写的排他锁，然后执行成功。**

lock in share mode是共享锁，不会阻塞读操作。



InnoDB是行级锁，在上面的例子里，如果上面的操作中会话2修改的是别的行，不会被阻塞。

```sql
#会话2修改id = 4的，不会和id=3的共享锁冲突，两者互不影响。
update table_name_innodb set 字段 = value where id = 4;
```



**同样，可以对读操作加排他锁**

```sql
select * from table_name_innodb where id = 3 for update;
```

这样的读操作加排他锁select for update之后，在执行完之前，对这一行的其他读、写操作都会被阻塞。



**InnoDB的行级锁是基于索引实现的。因此<font color=red>InnoDB的SQL没有用到索引的时候用的是表级锁，用到索引的时候用的是行级锁以及Gap锁(走普通非唯一锁时用到)。</font>**

此外，InnoDB的行级锁是针对索引加的锁，不针对数据记录，因此即使访问不同行的记录，如果使用了相同的索引键仍然会出现锁冲突。

在通过`SELECT ...LOCK IN SHARE MODE;`或`SELECT ...FOR UPDATE;`使用锁的时候，如果表没有定义任何索引，那么InnoDB会创建一个隐藏的聚簇索引并使用这个索引来加记录锁。



## MySQL锁原理

> MySQL InnoDB依靠多版本并发控制MVCC实现提交读和可重复读。

#### 当前读和快照读

当前读是对记录会加锁的操作，以下语句都是当前读：

```sql
select ... lock in share mode;  #这种是共享锁，其余都是排他锁
```

```sql
select ... for update  #排他锁
update  #增删改都属于当前读，因为在增删改之前都会先当前读来读取最新值，然后再增删改。
insert
delete
```

快照读是不加锁的非阻塞读

```sql
select
```

​	注意：在事务隔离级别不为串行化的情况下，select不加锁才成立。串行化情况下，也退化成当前读，即`select ... lock in share mode; `模式。

### InnoDB在RR隔离级别下的当前读就能解决幻读问题

理论情况下，MySQL RR隔离级别下应该是下面的场景，出现幻读。

```SQL
#session 1
start transaction;

#session 2 
start transaction;

#session 1 当前读，读取acount_innodb表的全部行
select * from acount_innodb lock in share mode;

#当前总共有3行，session 1读到了3行，id从1到3.
#session 2插入1行，上面事务1读取了整个表，这一行在事务1的操作范围内。
insert into account_innodb values(4,"newman",500);

#session 1更新所有余额为500 update操作也是当前读
update account_innodb set balance = 500;
#发现返回影响范围是4行，4行更新成功，出现了幻觉！ 
```

但是实际情况是insert操作被锁住了，需要等待session 1提交后才能插入。此时对于session1来讲，新增的数据行并没有出现，也就是**说InnoDB在RR级别下的当前读就可以避免幻读。原因是MVCC+next key锁**

要复现上面例子中的幻读问题，需要将innoDB的隔离级别降到RC隔离级别(读取已提交)。

在Serializable隔离级别下，对所有操作都会加锁，不需要加显式的锁，就可以解决幻读。如下：

```sql
set session transaction isolation level serializable;
#session 1
start transaction;

#session 2 
start transaction;

#session 1  Serializable隔离级别，不需要加锁！！！
select * from acount_innodb;

#session 2 下面的insert操作被阻塞，需要等待session1 commit之后才能执行成功，避免了幻读
insert into account_innodb values(4,"newman",500);
```



### MVCC实现RC，RR隔离级别

多版本并发控制（Multi-Version Concurrency Control, MVCC）是 MySQL 的 InnoDB 存储引擎实现**实现提交读和可重复读这两种隔离级别**的具体方式。

**核心：写操作更新最新的版本快照，而读操作去读旧版本快照，没有互斥关系，这一点和 CopyOnWrite 类似，避免了加锁操作，开销更低。**

对于读操作SELECT，会访问版本链中的记录，从最新的开始依次访问。如果是可重复读，只能读到在此事务开始前已经提交的记录；如果是提交读，只能读到已提交的结果。实现了提交读和可重复读。

**MVCC实现了RR、RC级别下的快照读——非阻塞读。**



#### 版本号

系统版本号 SYS_ID：是一个递增的数字，每开始一个新的事务，系统版本号就会自动递增。

事务版本号 TRX_ID ：事务开始时的系统版本号。每次进行修改操作的时候，都会记录下所在事务的这个TRX_ID。



#### Undo日志

**MVCC 的多版本指的是多个版本的快照，快照存储在 Undo 日志中，**在 MVCC 中事务的修改操作（DELETE、INSERT、UPDATE）会为数据行新增一个版本快照。Undo日志通过回滚指针 ROLL_PTR 把一个数据行的所有快照连接起来。

快照中除了记录事务版本号 TRX_ID 和操作之外，还记录了一个 bit 的 DEL 字段，用于标记是否被删除。

```sql
#当前事务版本号是50
insert id = 1, name = '小明1';   

#事务60进行修改
update table set name = '小明1' where id = 1  
```

此时在undo日志中就存在版本链：

![image-20210317142827019](images/MySQL/image-20210317142827019.png)

#### ReadView

MVCC 维护了一个 ReadView 结构，主要包含了当前系统未提交的事务列表 TRX_IDs {TRX_ID_1, TRX_ID_2, ...}，还有该列表的最小值 TRX_ID_MIN 和 TRX_ID_MAX。

在进行 SELECT 操作时，根据数据行快照的 TRX_ID 与 TRX_ID_MIN 和 TRX_ID_MAX 之间的关系，从而判断数据行快照是否可以使用：

- TRX_ID < TRX_ID_MIN，表示该数据行快照是在当前所有未提交事务之前进行更改的，因此可以使用。
- TRX_ID > TRX_ID_MAX，表示该数据行快照是在事务启动之后被更改的，因此不可使用。
- TRX_ID_MIN <= TRX_ID <= TRX_ID_MAX，需要根据隔离级别再进行判断：
  - **提交读：如果 TRX_ID 在 TRX_IDs 列表中，表示该数据行快照对应的事务还未提交，则该快照不可使用。**否则表示已经提交，可以使用。
  - **可重复读：都不可以使用。**因为如果可以使用的话，那么其它事务也可以读到这个数据行快照并进行修改，那么当前事务再去读这个数据行得到的值就会发生改变，也就是出现了不可重复读问题。

在数据行快照不可使用的情况下，需要沿着 Undo Log 的回滚指针 ROLL_PTR 找到下一个快照，再进行上面的判断。

简要来说：对于读操作SELECT，会访问版本链中的记录，从最新的开始依次访问。如果是可重复读，只能读到在此事务开始前已经提交的记录；如果是提交读，只能读到已提交的结果。



举例说明：假设当前列表里的事务id为[80，90，100]。

- 要访问的记录版本的事务id为50，比当前列表的TRX_ID_MIN80都小。说明这条记录在列表中所有事务开启之前就提交了，所以对当前活动的事务来说是可访问的。
- 要访问的记录版本的事务id为110，比事务列表的TRX_ID_MAX100都大，那说明这个条记录是在ReadView生成之后才发生的，所以不能被访问。
- 要访问的记录版本的事务id为80，在min与max之间，且在当前的列表中。说明当前事务还未提交，不能被访问。
- 如果要访问的记录版本是85，在min与max之间，但不在列表中，说明已提交。需要根据隔离级别判断
  - 如果是提交读，可以读。
  - 如果是可重复读，不能读。



**已提交读和可重复读的区别就在于它们生成ReadView的策略不同。已提交读隔离级别下的事务在每次查询的开始都会生成一个独立的ReadView，而可重复读隔离级别则在第一次读的时候生成一个ReadView，之后的读都复用之前的ReadView。**

举个例子 ，在已提交读隔离级别下：

提交上面例子中的50，60事务。此时有一个事务id为100的事务，修改了name,使得的name等于小明2，但是事务还没提交。则此时的版本链是：

![image-20210317142805264](images/MySQL/image-20210317142805264.png)

若此时另一个事务A发起了select 语句要查询id为1的记录，此时生成的ReadView 列表只有[100]。开始从版本链一条一条比较，首先找最近的一条，发现trx_id是100,也就是name为小明2的那条记录，发现在列表内，所以不能访问。这时候通过指针继续找下一条，name为小明1的记录，发现trx_id是60，小于列表中的最小id，所以可以访问，直接读取，结果为小明1。

如果这时候我们把事务id为100的事务提交了，并且新建了一个事务id为110也修改id为1的记录，并且不提交事务110。

```sql
110 BEGIN; 
UPDATE TABLE SET name = '小明3' WHERE id = 1;
```

 这时候版本链就是：

![image-20210317142747147](images/MySQL/image-20210317142747147.png)

这时候还是事务A又执行了一次查询,要查询id为1的记录。**关键的地方来了**

如果是**已提交读隔离级别，会重新创建一个ReadView**，活动事务列表中的值就变了，变成了[110]。通过trx_id对比版本链查找到合适的结果就是小明2。

如果是**可重复读隔离级别，这时候ReadView还是第一次select时候生成的ReadView，**也就是列表的值还是[100]。所以select的结果是小明1。所以第二次select结果和第一次一样，这样才能实现可重复读。



### Next-key Lock解决幻读问题

> **Next-key Lock会用到RR级别如下场景：**
>
> **使用主键索引，部分命中，会对主键索引加Next-key Lock。**
>
> **使用唯一索引，部分命中，会对主键索引和唯一索引加Next-key Lock。**
>
> **使用非唯一索引、普通索引，无论是否命中，都会对当前读的区间范围内加Next-key Lock。**
>
> **不适用索引，无论是否命中，都会对所有区间加Next-key Lock。类似表锁，但是代价比表锁大得多！！！**

由两部分组成，行锁+Gap锁。在RR和Serializable级别下都会开启，解决了幻读。

**Record Locks**
锁定一个记录上的索引，而不是记录本身。如果表没有设置索引，InnoDB 会自动在主键上创建隐藏的聚簇索引，因此 Record Locks 依然可以使用。



**Gap Locks**

锁定索引之间的间隙，但是不包含索引本身。例如当一个事务执行下面两个当前读语句，其它事务就不能table_a.id插入 15。

```sql
SELECT * FROM table_a WHERE id BETWEEN 10 and 20 FOR UPDATE;
SELECT * FROM table_a WHERE id BETWEEN 10 and 20 lock in share mode;
```

有两种方式显式关闭gap锁：

A. 将事务隔离级别设置为RC 

B. 将参数innodb_locks_unsafe_for_binlog设置为1



**Next-Key Locks**
它是 Record Locks 和 Gap Locks 的结合，不仅锁定一个记录上的索引，也锁定索引之间的间隙。它锁定一个前开后闭区间，例如一个索引包含以下值：10, 11, 13, and 20，那么就会按照如下划分区间：

```sql
(-∞, 10]
(10, 11]
(11, 13]
(13, 20]
(20, +∞)
```



**对于主键索引或者唯一索引：**

- 如果精确查询where条件全部命中，RC/RC都只会加记录锁。

- 如果精确查询或范围查询where条件部分命中或全部不命中，RR会加Next-key Lock。RC只会对命中记录加记录锁。

  唯一索引不但会锁唯一索引，还会锁主键。不然锁住唯一索引，另一个事务从主键索引把数据改了。

**如果是非唯一索引的当前读：**

- RR无论是否命中，都会加Next-key Lock锁定当前读所在的区间。

  如果不锁定区间，因为是非唯一索引，还可以往区间里面插入，会发生幻读。

举例说明：

存在如下表：name是primary key，id是普通索引。

```sql
name id
cc   6
b    9
d    9
f    11

delete from tb1 where id=9;
```

delete走的是非唯一索引，会把(6, 11]区间锁住，其他事务要插入数据，7，8，9，10，11都插不进去，5和12能插入进去。

考虑到主键索引，('a', 6)是能插入进去的，('e', 9)是插入不进去的。主键索引叶子结点按照首字母顺序排列a在区间外，e在区间内。

**如果不走索引，会对所有的Gap上锁，相当于锁表。**而且这会比锁表代价还大！！！





## 架构

### 如何设计一个数据

数据库的开发和我们做的大型项目一样，架构堪称经典，很有借鉴意义。

主要包括程序实例和存储（文件系统）两个部分。

程序实例包括：存储管理、缓存机制、SQL解析、日志管理、权限划分、容灾机制、索引管理、锁管理。

思考每一部分是怎么提出来的，有什么作用，能解决什么样的问题。常看D:\计算机\慕课\剑指Java面试\3-1 数据库架构.mp4

存储管理：类似操作系统的文件系统管理，存储的最小单位是块\页 



### 一条 SQL 语句在 MySQL 中如何执行的



### Redo Log、Undo Log、Bin Log