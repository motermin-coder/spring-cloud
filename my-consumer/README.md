### 1、客户端有四种弹性模式：
1、客户端负载均衡

2、断路器模式

3、后备模式

4、舱壁模式

这四种模式是在调用远程资源的客户端中实现的，它们的实现在逻辑上位于消费远程资源的客户端和资源之间。

断路器模式是模仿电路断路器的客户端弹性模式
有了断路器，当远程服务被调用时，断路器会监视这个调用。如果调用时间太长，断路器将会介入并中断调用。如果调用远程资源失败次数足够多，那么断路器将会采用快速失败，阻止将来调用失败的远程资源

后备模式就是当调用服务失败，服务消费者将会执行替代代码的路径，并返回替代代码的结果（个人觉得是备胎模式）

舱壁模式每次服务调用都是使用线程池的线程，线程之间不会影响，每次服务调用都是包装在线程中

断路器为远程调用提供的关键能力：
1、快速失败

2、优雅地失败

3、无缝恢复


### 2、Hystrix

添加了@HystrixCommand的注解，没有设置超时时间，则默认为1秒，则请求超过1秒，则断路器生效，让请求失败
可以单独设置超时时间，当调用方服务出现一些服务确实比其他服务耗时，可以将该服务调用隔离到单独的线程池中。
默认的情况下，所有的Hystrix命令都将共享同一个线程池来处理请求。这个线程池默认有10个线程来处理服务的调用

Hystrix的使用步骤：
1、添加相关依赖
```xml

<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-hystrix</artifactId>
</dependency>
<dependency>
    <groupId>com.netflix.hystrix</groupId>
    <artifactId>hystrix-javanica</artifactId>
    <version>1.5.9</version>
</dependency>
```
2、启动类添加相关注解
```java
@SpringBootApplication
@EnableEurekaClient
@EnableCircuitBreaker
public class MyConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyConsumerApplication.class, args);
    }

}
```
3、在service层添加相关注解，并添加后备代码
```java
@Service
public class ConsumerServiceImpl implements ConsumerService {


    @Autowired
    private RestTemplate restTemplate;



    public HashMap<String,Object> longTimeWork(){
        long l = System.currentTimeMillis();
        ArrayList<Object> objects = new ArrayList<>();
        for (int i = 0; i < 300; i++) {
            objects.add(i);
            try {
                Thread.sleep(10000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        long l1 = System.currentTimeMillis();
        HashMap<String, Object> map = new HashMap<>();
        map.put("list",objects);
        map.put("time",l1 - l);
        return map;
    }


    /**
     * 默认是1秒，超时1秒将出现调用服务失败，
     * 可以通过设置execution.isolation.thread.timeoutInMilliseconds来设置请求的最长时间，单位为毫秒，
     * fallbackMethod为调用服务失败的回调
     * @return
     */
    @HystrixCommand(
            fallbackMethod = "failProductList",
            commandProperties = {
                @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds",value = "10000")
            }
    )
    @Override
    public HashMap<String,Object> getProductList() {
        HashMap<String, Object> map = longTimeWork();
        HashMap forObject = restTemplate.getForObject("http://my-product-service/list", HashMap.class, (Object) null);
        forObject.put("other",map);
        return forObject;
    }

    public HashMap<String,Object> failProductList(){
        HashMap<String, Object> map = new HashMap<>();
        map.put("msg","超时失效了");
        return map;
    }
}
```

#### Hystrix自定义线程池
```java
@Service
public class ProductServiceImpl implements ProductService {

    /**
     * fallbackMethod为设置失败回调的方法，
     * threadPoolKey可以设定线程池的key
     * coreSize可以设定当前线程池组的最大线程
     * maxQueueSize可以设置当前线程池组的最大队列线程大小
     * 当请求来到，没有多余的线程工作，则添加到队列等待空闲线程来工作
     * @return
     */
    @HystrixCommand(
            fallbackMethod = "listProductFail",
            commandProperties = {
                    @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds",value = "10000")
            },
            threadPoolKey = "myThreadPoolKey",
            threadPoolProperties = {
                    @HystrixProperty(name = "coreSize",value = "30"),
                    @HystrixProperty(name = "maxQueueSize",value = "10")
            }
    )
    @Override
    public List<String> ListProduct() {
        ArrayList<String> strings = new ArrayList<>();
        for (int i = 0; i < 300; i++) {
            strings.add(i,"产品"+i);
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return strings;
    }

    /**
     * 调用服务失败的回调方法
     * @return
     */
    public List<String> listProductFail(){
        ArrayList<String> objects = new ArrayList<>();
        objects.add("调用服务超时了");
        return objects;
    }
}
```

#### 注意：
1、每当Hystrix命令遇到服务错误时，它将开始一个10秒的计时器，用于检查服务调用失败的频率。这个10秒的窗口是可以配置的。
2、错误阀值默认为50%，如果超过50%，Hystrix将会“跳闸”断路器。
3、Hystrix可以使用的配置级别有3个，分别是：应用、类、在类中定义的线程池级别。除非在线程级别上显示地覆盖，否则所有线程池都将继承应用级别的默认属性或类中的定义的默认属性。
4、当一个@HystrixCommand被执行，它可以使用两种隔离策略线程和信号量，默认的情况下以线程策略运行

