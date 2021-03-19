![MyBatis logo](images/MyBatis/mybatis-logo.png)

[MyBatis中文教程](https://mybatis.org/mybatis-3/zh/index.html)

[参考资料](D:\计算机\笔记\JavaGuide\docs\system-design\framework\mybatis\mybatis-interview.md)

[Github地址](https://github.com/mybatis/mybatis-3)

### 1. 什么是MyBatis？

1.Mybatis是一个半ORM（对象关系映射）框架，**它内部封装了JDBC**，开发时只需要关注SQL语句本身，不需要花费精力去处理加载驱动、创建连接、创建statement等繁杂的过程。程序员直接编写原生态sql，可以严格控制sql执行性能，灵活度高。

2.MyBatis 可以使用 XML 或注解来配置和映射原生信息，将对象(POJO)映射成数据库中的记录，避免了几乎所有的 JDBC 代码和手动设置参数以及获取结果集。

3.通过xml 文件或注解的方式将要执行的各种 statement 配置起来，并通过java对象和 statement中sql的动态参数进行映射生成最终执行的sql语句，最后由mybatis框架执行sql并将结果映射为java对象并返回。



### 2. MyBatis的优缺点

优点：

1. SQL写在XML里，解除sql与程序代码的耦合，便于统一管理，提高了可维护性。
2. 提供映射标签，支持对象与数据库的ORM字段关系映射；提供对象关系映射标签，支持对象关系组件维护。
3. 提供XML标签，支持编写动态SQL语句，并可重用。
4. 与JDBC相比，减少了50%以上的代码量，消除了JDBC大量冗余的代码，不需要手动开关连接。
5. 很好的与各种数据库兼容（因为MyBatis使用JDBC来连接数据库，所以只要JDBC支持的数据库MyBatis都支持）。
6. 能够与Spring很好的集成。 使用的人多！



缺点：

1. SQL语句的编写工作量较大，尤其当字段多、关联表多时，对开发人员编写SQL语句的功底有一定要求。

2. SQL语句依赖于数据库，导致数据库移植性差，不能随意更换数据库。



MyBatis框架适用场合：

1. MyBatis专注于SQL本身，是一个足够灵活的DAO层解决方案。

2. 对性能的要求很高，或者需求变化较多的项目，如互联网项目，MyBatis将是不错的选择。



### 3. MyBatis和Hibernate区别