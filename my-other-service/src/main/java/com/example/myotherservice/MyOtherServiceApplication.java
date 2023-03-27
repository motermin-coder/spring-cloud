package com.example.myotherservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;

@SpringBootApplication
@RestController
public class MyOtherServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyOtherServiceApplication.class, args);
    }

    @Value("${server.port}")
    private String port;

    @GetMapping("/list")
    public HashMap<String,Object> hello(){
        HashMap<String, Object> map = new HashMap<>();
        map.put("port",port);
        ArrayList<String> strings = new ArrayList<>();
        strings.add("hello world!");
        map.put("list",strings);
        map.put("msg","我是其他服务");
        return map;
    }
}
