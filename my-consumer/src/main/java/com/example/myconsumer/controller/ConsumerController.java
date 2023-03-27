package com.example.myconsumer.controller;

import com.example.myconsumer.service.ConsumerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
public class ConsumerController {



    @Autowired
    private ConsumerService consumerService;

    @DeleteMapping("/delete/{id}")
    public HashMap<String,Object> deleteOperate(@PathVariable Integer id){
        HashMap<String, Object> map = new HashMap<>();
        map.put("id",id);
        map.put("msg","删除id");
        return map;
    }

    @GetMapping("/other")
    public HashMap<String,Object> otherService(){
        return consumerService.otherService();
    }


    @GetMapping("/get/list")
    public HashMap<String, Object> getList(){
        return consumerService.getProductList();
    }

}
