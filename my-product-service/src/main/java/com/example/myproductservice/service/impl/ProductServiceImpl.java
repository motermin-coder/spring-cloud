package com.example.myproductservice.service.impl;

import com.example.myproductservice.service.ProductService;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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
        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ArrayList<String> strings = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            strings.add(i,"产品"+i);
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
