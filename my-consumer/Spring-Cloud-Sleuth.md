### 日志记录跟踪
可能实现分布式调试技术的几种方式，将关注以下内容：
1、使用关联ID将跨多个服务的事务链接一起
2、将来自多个服务的日志数据聚合在一个可搜索的源。
3、可视化跨多个服务的用户事务流，并理解事务每个部分的特性功能。

Spring-Cloud-Sleuth： 它将关联ID装备到HTTP调用上，并将生成的跟踪数据提供给OpenZipkin的钩子。
Spring-Cloud-Sleuth通过添加过滤器与其他Spring组件进行交互，将生成的关联ID传递到所有系统调用。

Papertrail：是一种基于云的服务，允许开发人员将来自多个源的日志数据聚合到单个可搜索的数据库中。

Zipkin: Zipkin是一种开源的可视化工具，可以显示跨多个服务的事务流。Zipkin允许开发人员将事务分解到它的组件块中，并可视化地识别存在性能热点的位置

#### 1、Spring-Cloud-Sleuth与关联ID
关联ID是一个随机生成的、唯一的数字或字符串，它在事务启动时分配给一个事务。当事务流过多个服务时，关联ID从一个服务调用传播到另一个服务调用。
有了Spring-Cloud-Sleuth，如果使用SpringBoot的日志实现，关联ID就会自动添加到微服务的日志语句中。


#### 2、剖析Spring-Cloud-Sleuth跟踪
Spring-Cloud-Sleuth将向每个日志条目添加以下4条信息：
1、服务的应用名称（默认的情况下为spring.application.name的值）
2、跟踪ID（跟踪ID是关联ID的等价术语，它表示整个事务的唯一编号）
3、跨度ID（跨度ID表示整个事务中某一部分的唯一ID）
4、是否将跟踪数据发送到Zipkin

#### 3、使用open Zipkin进行分布式追踪
Zipkin是一个分布式跟踪平台，用于跟踪跨多个服务调用的事务。
1、添加相关依赖项
```xml
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-sleuth-zipkin</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-sleuth</artifactId>
    </dependency>
```
2、application.yml添加如下配置
```yaml
spring:
  zipkin:
    base-url: http://localhost:9411
```
3、使用docker启动Zipkin服务器
```shell
docker run -d -p 9411:9411 openzipkin/zipkin
```
4、测试
本地浏览器访问localhost:9411