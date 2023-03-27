## Zuul
服务网关作为单个策略执行点，所有的调用都通过服务网关进行路由，最终被路由到最终目的地
服务网关充当服务客户端和被调用的服务之间的中介。服务网关充当应用程序内所有微服务调用的入站流量的守门人

可以在服务网关中实现的横切关注点包括以下几个：
1、静态路由
2、动态路由
3、验证和授权
4、度量数据收集和日志记录

Zuul的核心是一个反向代理

### 1、在Zuul中配置路由
有三种机制配置路由：
1、通过服务发现自动映射路由
可以向Eureka添加新服务，因为Zuul会与Eureka进行通信，了解实际服务的位置。可以通过Zuul服务器上的/routes端点来访问这些路由，这将返回服务中所有映射的列表。

2、使用服务发现手动映射路由
Zuul允许开发人员更细粒度地明确定义路由路径映射,可以添加自定义前缀
```yaml
zuul:
  routes:
    my-product-service: /my-product-service/**    # 加自定义映射路由
  #  ignored-services: 'my-consumer' # 排除基于Eureka的全部自动映射路由的y-consumer路由
  #  ignored-services: '*'     # 排除基于Eureka的全部自动映射路由
  prefix: /api    # 添加自定义前缀
```
3、使用静态URL手动映射路由
Zuul可以用来路由那些不受Eureka管理的服务。
对于非JVM应用程序，可以建立单独的Zuul服务器来处理这些路由。
```yaml
zuul:
  routes:
    other-service:
      path: /other-service/**
      url: http://localhost:8000
```

Zuul可以从Spring Cloud Config读取路由，Zuul公开了基于POST的端点路由/refresh,其作用是让重新加载路由配置

### 2、Zuul和服务超时
Zuul使用Netflix的Hystrix和Ribbon库，来帮助防止长时间运行的服务调用影响服务网关的性能。
在默认情况下，对于任何超时1秒的时间调用来处理请求，Zuul终将返回一个HTTP 500错误，这个情况下，
可以通过在Zuul服务器的配置中设置Hystrix超时属性来配置此行为。（设置了Hystrix超时，则需要考虑请求的超时是否大于Hystrix设置的超时）
```yaml
hystrix:
  command:
    my-product-service:     # my-product-service为服务名
      execution:
        isolation:
          thread:
            timeoutInMilliseconds: 15000  # 单位为毫秒
my-product-service: # my-product-service为服务名
  ribbon:
    ReadTimeOut: 15000
```
对于超过5秒的配置，必须同时设置Hystrix和Ribbon超时

### 3、Zuul的使用
1、添加相关依赖
```xml
    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-zuul</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-eureka</artifactId>
        </dependency>
        <dependency>
            <groupId>org.apache.httpcomponents</groupId>
            <artifactId>httpclient</artifactId>
            <version>4.5.2</version>
        </dependency>
    </dependencies>
```
2、主类添加注解
```java
@SpringBootApplication
@EnableZuulProxy
public class ZuulApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZuulApplication.class, args);
    }
    
}
```
3、服务调用者修改路径指向Zuul网关
```java
@Service
public class ConsumerServiceImpl implements ConsumerService {
    
    @Autowired
    private RestTemplate restTemplate;
    
    /**
     * 网关服务名为： zuul-gateway
     * 请求的服务名为： other-service
     * 请求服务名的端点为： /list
     * @return
     */
    @Override
    public HashMap<String, Object> otherService() {
        return restTemplate.getForObject("http://zuul-gateway/other-service/list", HashMap.class, (Object) null);
    }
    
}
```

### 4、Zuul过滤器
Zuul过滤器可以按照与J2EE Servlet过滤器或者Spring Aspect类似的方式来使用。
Zuul支持以下三种类型的过滤器：
1、前置过滤器：前置过滤器在Zuul将实际请求发送到目的地之前被调用
2、后置过滤器：后置过滤器在目标服务被调用之前的并将响应发送回客户端后被调用
3、路由过滤器：路由过滤器用于在调用目标服务之前拦截调用（比如：有新版本体验时候，可以根据随机数与新旧版本的服务实例的赞比决定路由到那个服务实例）

路由过滤器可以将服务调用重定向到Zuul服务器被配置的发送路由以外的位置。但是Zuul路由过滤器不会执行HTTP重定向，而是会终止传入的HTTP请求。


#### 1、构建前置过滤器
```java
@Component
public class TrackingFilter extends ZuulFilter {

    private static final int FILTER_ORDER = 1;
    private static final boolean SHOULD_FILTER = true;
    private static final Logger log = LoggerFactory.getLogger(TrackingFilter.class);

    @Autowired
    FilterUtils filterUtils;


    @Override
    public String filterType() {
        return FilterUtils.PRE_FILTER_TYPE;
    }

    @Override
    public int filterOrder() {
        return FILTER_ORDER;
    }

    @Override
    public boolean shouldFilter() {
        return SHOULD_FILTER;
    }

    private boolean isCorrelationIdPresent(){
        if (filterUtils.getCorrelationId() != null){
            return true;
        }
        return false;
    }

    private String generateCorrelationId(){
        return UUID.randomUUID().toString();
    }

    @Override
    public Object run() {
        if (isCorrelationIdPresent()){
            log.info("前置拦截中发现 tmx-correlation-id: {}",filterUtils.getCorrelationId());
        } else {
            filterUtils.setCorrelationId(generateCorrelationId());
            log.info("tmx-correlation-id 在前置拦截器中生成了：{}",filterUtils.getCorrelationId());
        }
        RequestContext currentContext = RequestContext.getCurrentContext();
        log.info("处理传入的请求 {}", currentContext.getRequest().getRequestURI());
        return null;
    }
}
```

#### 2、构建后置过滤器
```java
@Component
public class ResponseFilter extends ZuulFilter {

    private static final int FILTER_ORDER = 1;
    private static final boolean SHOULD_FILTER = true;
    private static final Logger logger = LoggerFactory.getLogger(ResponseFilter.class);

    @Autowired
    FilterUtils filterUtils;



    @Override
    public String filterType() {
        return FilterUtils.POST_FILTER_TYPE;
    }

    @Override
    public int filterOrder() {
        return FILTER_ORDER;
    }

    @Override
    public boolean shouldFilter() {
        return SHOULD_FILTER;
    }

    @Override
    public Object run() {
        RequestContext currentContext = RequestContext.getCurrentContext();
        logger.info("Adding the correlation id to the outbound headers. {}", filterUtils.getCorrelationId());
        currentContext.getResponse().addHeader(FilterUtils.CORRELATION_ID,filterUtils.getCorrelationId());
        logger.info("Completing outgoing request for {}.",currentContext.getRequest().getRequestURI());
        return null;
    }
}
```