### Spring Cloud Stream的事件驱动架构
使用异步消息在应用程序之间进行通信并不新鲜，新鲜的是使用消息实现事件通信的概念，这些事件代表了事件的变化。这个概念称为事件驱动架构，也被称为消息驱动架构。基于事件驱动的方法允许开发人员构建高度解耦的系统，它可以对变更做出反应，而不需要与特定的库或服务紧密耦合。

消息队列在微服务之间充当中介，它有4个好处：
1、松耦合
2、耐久性
3、可伸缩性
4、灵活性

#### 1、Spring Cloud Stream架构
有四个组件涉及发布消息和消费消息：
1、发射器
2、通道
3、绑定器
4、接收器

#### 2、使用Spring Cloud Stream的步骤
1、在发送者微服务中添加相关依赖
```xml
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-stream</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-stream-kafka</artifactId>
    </dependency>
```
2、在发送者微服务中编写发射器
```java
@Component
@EnableBinding(Source.class) // 绑定的是Source接口，是Spring Cloud定义的一个接口
public class SimpleSourceBean {


    private Source source;

    private static final Logger logger = LoggerFactory.getLogger(SimpleSourceBean.class);

    @Autowired
    public SimpleSourceBean(Source source){
        this.source = source;
    }

    public void PublishMessage(String action,Long id){
        logger.debug("发送消息到kafka: action = {}, id = {}",action,id);
        HashMap<String, Object> map = new HashMap<>();
        map.put("action",action);
        map.put("id",id);
        map.put("date",new Date());
        map.put("channel","out");
        // 绑定的是Source接口，是Spring Cloud定义的一个接口，它有一个output()方法
        source.output().send(MessageBuilder.withPayload(map).build());
    }
}

@RestController
public class MessageSendController {

    @Autowired
    private SimpleSourceBean simpleSourceBean;
    
    /**
     * 删除操作
     * @param id
     * @return
     */
    @DeleteMapping("/delete/{id}")
    public HashMap<String,Object> delOperation(@PathVariable Long id){
        simpleSourceBean.PublishMessage("add",id);
        HashMap<String, Object> map = new HashMap<>();
        map.put("msg","成功");
        map.put("code",200);
        return map;
    }
}
```
3、在发送者微服务中修改application.yml相关配置
```yaml
spring:
  application:
    name: message-send-service
  cloud:
    stream:
      bindings:
        output:
          destination: message-receive-service # 输出通道output映射的topic为message-receive-service
          content-type: application/json # 发送消息的格式为JSON
      kafka:
        binder:
          zk-nodes: localhost # kafka的位置为本地
          brokers: localhost # zookeeper的位置为本地
```
4、在接收者微服务中添加相关依赖
```xml
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-stream</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-stream-kafka</artifactId>
    </dependency>
```
5、在接受者微服务中添加接受监听器
```java
@EnableBinding(Sink.class)  // Sink也是Spring Cloud的接口，它有个input()
public class InputChannelListener {

    private static final Logger logger = LoggerFactory.getLogger(InputChannelListener.class);

    /**
     * 接收input通道的消息
     * @param hashMap
     */
    @StreamListener(Sink.INPUT)
    public void loggerSink(HashMap<String,Object> hashMap){
        logger.info("接受的内容为： {}", hashMap);
    }
}
```
6、在接收者微服务中修改application.yml相关配置
```yaml
spring:
  application:
    name: message-receive-service
  cloud:
    stream:
      bindings:
        input:
          destination: message-receive-service # 将input通道的映射到message-receive-service队列
          content-type: application/json # 接收消息类型
          group: messageGroup # 定义消费组名称
      kafka:
        binder:
          zk-nodes: localhost # kafka的位置为本地
          brokers: localhost # zookeeper的位置为本地
```

### 3、使用自定义通道发送、接收消息
1、发送者微服务编写发送通道接口类
```java
public interface CustomChannels {
    // 发送通道的名字为myChannelOut
    @Output("myChannelOut")
    MessageChannel myChannelOut();
}
```
2、发送者微服务编写发射器
```java
@Component
@EnableBinding(CustomChannels.class) // CustomChannels类为自定义的发送通道接口类
public class CustomSourceBean {

    private CustomChannels customChannels;

    private static final Logger logger = LoggerFactory.getLogger(CustomSourceBean.class);

    @Autowired
    public CustomSourceBean(CustomChannels customChannels){
        this.customChannels = customChannels;
    }

    public void publishMessage(String action,Long id){
        logger.debug("发送消息到kafka: action = {}, id = {}",action,id);
        HashMap<String, Object> map = new HashMap<>();
        map.put("action",action);
        map.put("id",id);
        map.put("date",new Date());
        map.put("channel","myChannelOut");
        customChannels.myChannelOut().send(MessageBuilder.withPayload(map).build());
    }
}
```
3、发送者微服务application.yml添加新的通道映射
```yaml
spring:
  application:
    name: message-send-service
  cloud:
    stream:
      bindings:
        output:
          destination: message-receive-service # 输出通道output映射的topic为message-receive-service
          content-type: application/json # 发送消息的格式为JSON
        myChannelOut:
          destination: custom-message-receive-service # 输出通道myChannelOut映射的topic为custom-message-receive-service
          content-type: application/json # 发送消息的格式为JSON
      kafka:
        binder:
          zk-nodes: localhost # kafka的位置为本地
          brokers: localhost # zookeeper的位置为本地
```
4、接收者微服务编写接收通道接口类
```java
public interface CustomChannels {
    // 接收通道的名字为myChannelIn
    @Input("myChannelIn")
    SubscribableChannel myChannelIn();
}
```
5、接收者微服务编写接收监听器
```java
@EnableBinding(CustomChannels.class) // CustomChannels类为自定义的接收通道接口类
public class CustomChannelListener {

    private static final Logger logger = LoggerFactory.getLogger(CustomChannelListener.class);
    
    // 接收通道的名字为myChannelIn
    @StreamListener("myChannelIn")
    public void listener(HashMap hashMap){
        logger.info("自定义通道myChannelIn接收的消息： {}", hashMap);
    }
}
```
6、接收者微服务application.yml添加新的通道映射
```yaml
spring:
  application:
    name: message-receive-service
  cloud:
    stream:
      bindings:
        input:
          destination: message-receive-service # 将input通道的映射到message-receive-service队列
          content-type: application/json # 接收消息类型
          group: messageGroup # 定义消费组名称
        myChannelIn:
          destination: custom-message-receive-service # 将myChannelIn通道的映射到custom-message-receive-service队列
          content-type: application/json # 接收消息类型
          group: customMessageGroup # 定义消费组名称
      kafka:
        binder:
          zk-nodes: localhost # kafka的位置为本地
          brokers: localhost # zookeeper的位置为本地
```

> 可以自定义多个发送、接收通道，可以定义多个通道和队列的映射