## IOC

**[零基础带你看Spring源码——IOC控制反转](https://mp.weixin.qq.com/s?__biz=MzIyODI2NDE3Nw==&mid=2650127780&idx=1&sn=df75f0278df2910c5c8ef03741dfda06&chksm=f0558c3ac722052c620183fb901f08139dd095bb83081eb6b468695d0ed933a4960daffa94c0&token=1123336669&lang=zh_CN#rd)**

IOC（控制反转）：获得依赖对象的方式反转了，原来对象的创建由程序自己控制，控制反转后将对象的创建转移给第三方。

IOC是一种编程思想，由主动编程变成了被动接收。

在Spring中实现控制反转的是IoC容器，其实现方法是依赖注入（Dependency Injection,DI）。

依赖注入：就是利用set方法来进行注入的。

一句话搞定：对象是由Spring创建，管理，装配！



## 注解

### 注入属性

相当于bean标签中的`<property>`

**@Autowired：先`byType`装配，如果有多个相同类型的bean，按照变量名称注入。**

**@Qualifier：结合@Autowired使用**，在按照类型的基础上，**注入指定名称的bean**。`@Qualifier(value = "happyDog")`

**@Resource：先按名称注入，再按类型注入。按名称注入，直接按照bean的id注入。**`@Resource(name = "happyDog")`