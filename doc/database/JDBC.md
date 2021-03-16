> 狂神b站JDBC上课笔记

**数据库驱动**

我们安装好数据库之后，我们的应用程序也是不能直接使用数据库的，必须要通过相应的数据库驱动程序，通过驱动程序去和数据库打交道。类似于显卡驱动，等等。

不同的数据库拥有不同的驱动，而JDBC屏蔽了这些驱动的不同。



## JDBC的作用

JDBC即Java DataBase Connectivity，java数据库连接；JDBC为多种关系数据库提供统一访问，它提供的API可以让Java通过API方式访问关系型数据库，执行SQL语句，获取数据。从根本来讲，JDBC是一种规范，它提供的接口，是一套完整的、可移植的访问底层数据库的程序。

![image-20210315104248912](images/JDBC/Jdbc作用.png)

屏蔽了不同的数据库厂商的差异性，数据库厂商去实现Jdbc接口，开发人员只需要学Jdbc即可。

执行流程：

- 连接数据源。
- 为数据库传递查询和更新指令。
- 处理数据库响应并返回结果。



## 第一个JDBC程序

**导入数据库驱动**

导入MySQL的驱动（用什么数据库导入什么数据库驱动即可），`Project Structure-modules-dependencise-jar or directories`导入`mysql-connector-java-5.1.49-bin.jar`。

或者`Project Structure-Libraries-+-Java-选择要导入的jar包`

**创建测试数据库**

```sql
CREATE DATABASE jdbcStudy CHARACTER SET utf8 COLLATE utf8_general_ci;

USE jdbcStudy;

CREATE TABLE `users`(
id INT PRIMARY KEY,
NAME VARCHAR(40),
PASSWORD VARCHAR(40),
email VARCHAR(60),
birthday DATE
);

INSERT INTO `users`(id,NAME,PASSWORD,email,birthday)
VALUES(1,'zhansan','123456','zs@sina.com','1980-12-04'),
(2,'lisi','123456','lisi@sina.com','1981-12-04'),
(3,'wangwu','123456','wangwu@sina.com','1979-12-04');
```

**编写测试代码**

总的步骤：

1. 加载驱动 Class.forName
2. 连接数据库 DriverManager.getConnection
3. 获得执行SQL的对象 statement(不安全)
4. 获得返回的结果集
5. 释放连接

```JAVA
import java.sql.*;

public class JdbcFirstDemo {
   public static void main(String[] args) throws Exception {
        //1. 加载驱动
        Class.forName("com.mysql.jdbc.Driver");

        //2. 用户信息和url
        String url = "jdbc:mysql://localhost:3306/jdbcstudy?useSSL=false&useUnicode=true&characterEncoding=utf8";
        String username = "root";
        String password = "123456";

        //3. 连接成功 数据库对象
        Connection connection = DriverManager.getConnection(url, username, password);

        //4. statement-执行SQL的对象 
        Statement statement = connection.createStatement();

        //5. 执行SQL的对象 去执行SQL 可能存在结果，查看返回结果
        String sql = "select * from users";
        //返回的结果集
        ResultSet resultSet = statement.executeQuery(sql);
        while (resultSet.next()) {
            System.out.println("id=" + resultSet.getObject("id"));
            System.out.println("name=" + resultSet.getObject("name"));
        }

        //6. 释放连接 
        resultSet.close();
        statement.close();
        connection.close();
    }
}
```

==释放连接必须做，因为这些连接资源特别占用内存资源，特别是connection。==



## JDBC中的对象

### DriverManager

第一步：加载驱动时使用`Class.forName("com.mysql.jdbc.Driver");`

不需要使用``DriverManager.registerDriver(new Driver());`的方式注册驱动，因为使用`Class.forName`，Driver类中的静态代码块会被执行：

```java
public class Driver extends NonRegisteringDriver implements java.sql.Driver {
    public Driver() throws SQLException {
    }

    static {
        try {
            DriverManager.registerDriver(new Driver());
        } catch (SQLException var1) {
            throw new RuntimeException("Can't register driver!");
        }
    }
}
```



### URL

```java
String url = "jdbc:mysql://localhost:3306/jdbcstudy?useSSL=false&useUnicode=true&characterEncoding=utf8";
//mysql--3306
// 协议://主机地址:端口号/数据库名?参数1&参数2...

//oracle--1521
//jdbc:oracle:thin:@localhost:1521:sid   //Oracle没有数据库，全是表
```



### Connection

Connection代表数据库

```java
connection.rollback();
connection.rollback();
connection.setAutoCommit();
```



### Statement

执行SQL的对象，因此是最重要的一个。

```java
//最主要的两个方法executeUpdate、executeQuery
//INSERT, UPDATE, or DELETE都是用这个，返回受影响的行数   还有DDL--CREATE、ALTER、DROP
int num = statement.executeUpdate(sql);
if(num > 0){
    System.out.println("插入/删除/更新成功");
}

//查询操作 返回结果集ResultSet
ResultSet resultSet = statement.executeQuery(sql);

//可以执行任何sql
statement.execute();

//批量执行 
int[] executeBatch() throws SQLException;
```



### ResultSet

查询语句，会返回结果集。

```java
//不知道列类型就用Object
resultSet.getObject();

//如果知道列的类型就使用对应类型获取

resultSet.getString();
resultSet.getInt();
resultSet.getFloat();
resultSet.getDouble();
...

//移动到下一行数据，如果当前行valid，返回true。
boolean next() throws SQLException;

resultSet.beforeFirst();//移动到最前
resultSet.afterLast();//移动到最后
resultSet.previous();//移动到前一行
resultSet.absolute(row);//移动到第row行
```



## Statement——JDBC增删改查详解

1、在src目录下创建db.properties文件存储数据库信息，降低耦合。

```properties
driver=com.mysql.jdbc.Driver
url=jdbc:mysql://localhost:3306/jdbcstudy?useSSL=false&useUnicode=true&characterEncoding=utf8
username=root
password=123456
```

2、提取工具类，复用加载驱动，建立连接，释放连接。

```java
package com.mkx.learn.jdbc.lesson2.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

public class JdbcUtils {
    private static String driver = null;
    private static String url = null;
    private static String username = null;
    private static String password = null;

    static {
        try {
            InputStream in = JdbcUtils.class.getClassLoader().getResourceAsStream("db.properties");
            Properties properties = new Properties();
            assert in != null;
            properties.load(in);
            driver = properties.getProperty("driver");
            url = properties.getProperty("url");
            username = properties.getProperty("username");
            password = properties.getProperty("password");
            //1. 加载驱动
            Class.forName(driver);

        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
    }

    //获取连接
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    //施放资源
    public static void release(Connection connection, Statement statement, ResultSet resultSet) throws Exception {
        if (resultSet != null) {
            resultSet.close();
        }
        if (statement != null) {
            statement.close();
        }
        if (connection != null) {
            connection.close();
        }
    }
}
```

3、增删改Test `executeUpdate`

```java
package com.mkx.learn.jdbc.lesson2;

import com.mkx.learn.jdbc.secondClass.util.JdbcUtils;
import java.sql.*;

public class Test {
    public static void main(String[] args) throws Exception {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            //1. 获取数据库连接
            connection = JdbcUtils.getConnection();

            //2. 获取执行SQL的对象
            statement = connection.createStatement();

            String sql = Test.testUpdate();

            if (statement.executeUpdate(sql) > 0) {
                System.out.println("执行！");
            }

            sql = "select * from users";
            //查询语句，用executeQuery
            resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                System.out.println("id=" + resultSet.getObject("id"));
                System.out.println("name=" + resultSet.getObject("name"));
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            JdbcUtils.release(connection, statement, resultSet);
        }
    }


    private static String testInsert() {
        return "insert into users(id,name,password,email,birthday)" +
                "values(4,'tzt','123456','123456@qq.com','1998-08-08');";
    }

    private static String testDelete() {
        return "delete from users where id = 4";
    }

    private static String testUpdate() {
        return "update users set name = 'mkx', email = '123456789@qq.com' where id = 1";
    }
}
```



## SQL注入问题

> SQL注入即是指web应用程序**对用户输入数据的合法性没有判断或过滤不严**，攻击者可以在web应用程序中事先定义好的**查询语句的结尾上添加额外的SQL语句**，在管理员不知情的情况下实现非法操作，以此来实现欺骗数据库服务器执行非授权的任意查询，从而进一步得到相应的数据信息。


比如登录业务中，需要查询账号密码所对应的用户（比对），可能用到如下sql语句：

```sql
select * from users where name='name';
```

其中name和password两个变量都是用户所传入的数据。

Java中SQL如果这样写：

```java
String sql = "select * from users where name='" + name + "'";
```

如果用户构造合适的输入，比如：

```java
String name=" ' or  '1=1";
```

那么如上sql语句拼接成了：

```sql
select * from users where name=' ' or '1=1'
```

**会匹配出表中所有用户的信息！！！！**



如何解决SQL注入问题？使用PreparedStatement对象。



## PreparedStatement

可以防止SQL注入，而且效率更高。因此推荐使用PreparedStatement

PreparedStatement防止SQL注入的本质：

**把传进来的参数当作字符。**如果其中存在符号，会被转义。比如：`String name=" ' or  '1=1";`，字符串内的单引号会被转义，整个字符串会被当作一个字符串来使用，**name==后面的整体才可以**。

```java
package com.mkx.learn.jdbc.lesson3;

import com.mkx.learn.jdbc.lesson2.util.JdbcUtils;

import java.sql.*;

public class PreparedStatementDemo {

    public static void main(String[] args) throws Exception {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            connection = JdbcUtils.getConnection();
            
            //插入
            //用 ？占位符
            String sql = "insert into users(id,name,password,email,birthday) values(?,?,?,?,?);";
            //和Statement的区别：直接把sql作为参数放入
            statement = connection.prepareStatement(sql);
            //手动给参数赋值  set函数的第一个参数表示第几个?
            statement.setInt(1, 7);
            statement.setString(2, "hhh");
            statement.setString(3, "12312313");
            statement.setString(4, "15612318@qq.com");
            //注意java Date和sql.Date不一样
            statement.setDate(5, new java.sql.Date(System.currentTimeMillis()));
            //executeUpdate()才会执行
            if (statement.executeUpdate() > 0) {
                System.out.println("插入成功！");
            }

            //删除
            sql = "delete from users where id=?;";
            statement = connection.prepareStatement(sql);
            //第一列：id = 3  where id = 3
            statement.setInt(1, 2);
            if (statement.executeUpdate() > 0) {
                System.out.println("删除成功！");
            }

            //修改
            sql = "update users set `NAME`=? where id=?;";
            statement = connection.prepareStatement(sql);
            //第一列：id = 3  where id = 3
            statement.setString(1, "凯翔");
            statement.setInt(2, 5);
            if (statement.executeUpdate() > 0) {
                System.out.println("更新成功！");
            }

            //查询 使用executeQuery()
            sql = "select * from users where id=?;";
            statement = connection.prepareStatement(sql);
            statement.setInt(1, 1);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                System.out.println(resultSet.getString("NAME"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            JdbcUtils.release(connection, statement, resultSet);
        }
    }
}
```



## JDBC操作事务

开启事务：`connection.setAutoCommit(false);`      //关闭自动提交 会自动开启事务

一组SQL执行完毕，提交事务` connection.commit();`

可以在catch语句种显式地定义回滚语句，但是默认失败会自动回滚，并不需要显式回滚。

```java
import utils.JdbcUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Test3 {
    public static void main(String[] args) throws Exception {
        Connection connection=null;
        PreparedStatement statement=null;
        ResultSet resultSet=null;
        try{
            connection= JdbcUtils.getConnection();
            connection.setAutoCommit(false);//关闭自动提交 开启事务
            String sql1="update users set name='hhh' where name='hyx';";
            String sql2="update users set name='tzt' where name='ttt';";
            
            //int i=1/0; 报错
            
            statement=connection.prepareStatement(sql1);
            statement.executeUpdate();
            statement=connection.prepareStatement(sql2);
            statement.executeUpdate();

            //业务完毕提交事务
            connection.commit();
            System.out.println("成功");

        }catch (Exception e){
            e.printStackTrace();
            //如果失败则回滚
            connection.rollback();
        }finally {
            JdbcUtils.release(connection,statement,resultSet);
        }
    }
}
```



## 数据库连接池

数据库连接池
数据库链接–执行完毕–施放十分消耗资源
**池化技术：准备一些预先的资源，过来就链接预先准备好的**



### 数据库连接池基础

#### 优点

1、资源重用 (连接复用)

   由于数据库连接得到重用，避免了频繁创建、释放连接引起的大量性能开销。在减少系统消耗的基础上，增进了系统环境的平稳性（减少内存碎片以级数据库临时进程、线程的数量）

2、更快的系统响应速度

   数据库连接池在初始化过程中，往往已经创建了若干数据库连接置于池内备用。此时连接池的初始化操作均已完成。对于业务请求处理而言，直接利用现有可用连接，避免了数据库连接初始化和释放过程的时间开销，从而缩减了系统整体响应时间。

3、统一的连接管理，避免数据库连接泄露

   在较为完备的数据库连接池实现中，可根据预先的连接占用超时设定，强制收回被占用的连接，从而避免了常规数据库连接操作中可能出现的资源泄露



#### 一般配置

最小连接数：是连接池一直保持的数据库连接，所以如果应用程序对数据库连接的使用量不大，将会有大量的数据库连接资源被浪费。
最大连接数：是连接池能申请的最大连接数，如果数据库连接请求超过此数，后面的数据库连接请求将被加入到等待队列中，这会影响之后的数据库操作。
等待超时：等待时间超过一定值直接失败



#### 如何编写连接池

**实现javax.sql.DataSource接口**

```java

public interface DataSource  extends CommonDataSource, Wrapper {
  Connection getConnection() throws SQLException;
  Connection getConnection(String username, String password) throws SQLException;
  ...  
}
```



### 开源数据源实现

> 拿来即用

DBCP
C3P0
Druid：阿里巴巴  Springboot部分会讲  有监控

使用了数据库连接池之后，我们在项目开发中就不需要编写连接数据库的代码了！即加载驱动和连接数据库这两步就不用我们自己做了。



#### DBCP

需要导入的包：

commons-dbcp-1.4、commons-pool-1.6

```properties
#driverClassName这个名字是dbcp数据源种定义好的，写死的，不能改变
driverClassName=com.mysql.jdbc.Driver  
url=jdbc:mysql://localhost:3306/jdbcstudy?useSSL=false&useUnicode=true&characterEncoding=utf8
username=root
password=123456

#可以设置最小连接数、最大连接等参数  也可以全部不设置，用默认配置
```



只需要修改utils，SQL代码部分不变。

**修改工具类utils**

​	对比之前的加载驱动，建立连接的写法，不再需要自己建联，直接用dbcp提供的工厂创建数据源：

​		` dataSource=BasicDataSourceFactory.createDataSource(properties);`

​	获取连接：`dataSource.getConnection();`

​	关闭资源部分也不变。

```java
package utils;

import org.apache.commons.dbcp.BasicDataSourceFactory;

import javax.sql.DataSource;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

public class JdbcUtils_DBCP {
    private static DataSource dataSource=null;

    static {
        try {
            InputStream in = dbcUtils.class.getClassLoader().
                				getResourceAsStream("dbcp.properties");
            Properties properties=new Properties();
            assert in != null;
            properties.load(in);

            //创建数据源 工厂模式→创建
            dataSource=BasicDataSourceFactory.createDataSource(properties);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //获取连接
    public static Connection getConnection() throws Exception {
        return dataSource.getConnection();
    }

    //施放资源  不变
    public  static void release(Connection connection, Statement statement, ResultSet resultSet) throws Exception {
        if (resultSet != null) {
            resultSet.close();
        }
        if (statement != null) {
            statement.close();
        }
        if (connection != null) {
            connection.close();
        }

    }
}
```



#### C3P0

需要导入的包：

c3p0-0.9.5.5.jar、mchange-common-java-0.2.19.jar

c3p0使用xml配置或setter配置。配置相关可以在网上搜 [博客1](https://blog.csdn.net/zhanghanlun/article/details/80918422)

##### xml配置文件配置

src目录下创建：c3p0-config.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<c3p0-config>
    <!-- 
		默认配置，如果不写参数，则使用这个配置 
		ComboPooledDataSource dataSource = new ComboPooledDataSource();
	-->
    <default-config>
        <property name="user">zhanghanlun</property>
        <property name="password">123456</property>
        <property name="jdbcUrl">jdbc:mysql://localhost:3306/zhanghanlun</property>
        <property name="driverClass">com.mysql.jdbc.Driver</property>
        <property name="checkoutTimeout">30000</property>
        <property name="idleConnectionTestPeriod">30</property>
        <!-- 初始化数据库连接池时连接的数量 -->
        <property name="initialPoolSize">3</property>
        <property name="maxIdleTime">30</property>
        <!-- 数据库连接池中的最大的数据库连接数 -->
        <property name="maxPoolSize">100</property>
         <!-- 数据库连接池中的最小的数据库连接数 -->
        <property name="minPoolSize">2</property>
        <property name="maxStatements">200</property>
    </default-config>
    
    <!-- 
		命名的配置,可以通过方法调用实现 
		ComboPooledDataSource dataSource = new ComboPooledDataSource("mysql");
		ComboPooledDataSource dataSource = new ComboPooledDataSource("oracle");
		....
	-->
    <named-config name="mysql">
        <property name="user">zhanghanlun</property>
        <property name="password">123456</property>
        <property name="jdbcUrl">jdbc:mysql://localhost:3306/zhanghanlun</property>
        <property name="driverClass">com.mysql.jdbc.Driver</property>
        <!-- 如果池中数据连接不够时一次增长多少个 -->
        <property name="acquireIncrement">5</property>
        <property name="initialPoolSize">20</property>
        <property name="maxPoolSize">25</property>
        <property name="minPoolSize">5</property>
    </named-config>
</c3p0-config>
```

```java
public class JdbcUtils_C3P0 {
    private static DataSource dataSource=null;

    static {
        try {
            //xml不需要load，会自动加载
            //创建数据源 工厂模式→创建
            dataSource = new ComboPooledDataSource();

        }catch (Exception e){
            e.printStackTrace();
        }
    }
    
    //获取连接 不变
    public static Connection getConnection() throws Exception {
        return dataSource.getConnection();
    }

    //施放资源  不变
    public  static void release(Connection connection, Statement statement, ResultSet resultSet) throws Exception {
        if (resultSet != null) {
            resultSet.close();
        }
        if (statement != null) {
            statement.close();
        }
        if (connection != null) {
            connection.close();
        }

    }
}
```

##### setter配置 不建议

```java
	private static ComboPooledDataSource dataSource;
	static {
        try {
            dataSource = new ComboPooledDataSource();
            dataSource.setDriverClass("com.mysql.jdbc.Driver");
            dataSource.setJdbcUrl("jdbc:mysql://localhost:3306/zhanghanlun");
            dataSource.setUser("zhanghanlun");
            dataSource.setPassword("123456");
            dataSource.setInitialPoolSize(3);
            dataSource.setMaxPoolSize(10);
            dataSource.setMinPoolSize(3);
            dataSource.setAcquireIncrement(3);
        } catch (PropertyVetoException e) {
            e.printStackTrace();
        }
    }
```



#### 结论

无论使用什么数据源，本质是一样的。DataSource接口不变，方法就不会改变。

`dataSource.getConnection();`