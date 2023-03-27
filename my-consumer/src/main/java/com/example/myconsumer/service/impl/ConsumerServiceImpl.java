package com.example.myconsumer.service.impl;

import com.example.myconsumer.service.ConsumerService;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class ConsumerServiceImpl implements ConsumerService {


//    @Autowired
//    private RestTemplate restTemplate;

    @Autowired
    private OAuth2RestTemplate restTemplate;



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
     * 网关服务名为： zuul-gateway
     * 请求的服务名为： other-service
     * 请求服务名的端点为： /list
     * @return
     */
    @Override
    public HashMap<String, Object> otherService() {
        return restTemplate.getForObject("http://zuul-gateway/other-service/list",HashMap.class, (Object) null);
    }

    /**
     * 默认是1秒，超时1秒将出现调用服务失败，
     * 可以通过设置execution.isolation.thread.timeoutInMilliseconds来设置请求的最长时间，单位为毫秒，
     * fallbackMethod为调用服务失败的回调
     * @return
     */
//    @HystrixCommand(
//            fallbackMethod = "failProductList",
//            commandProperties = {
//                @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds",value = "10000")
//            }
//    )
    @Override
    public HashMap<String,Object> getProductList() {
//        HashMap<String, Object> map = longTimeWork();
        HashMap forObject = restTemplate.getForObject("http://localhost:9010/api/my-product-service/list", HashMap.class, (Object) null);
//        forObject.put("other",map);
        return forObject;
    }

    public HashMap<String,Object> failProductList(){
        HashMap<String, Object> map = new HashMap<>();
        map.put("msg","超时失效了");
        return map;
    }
}
