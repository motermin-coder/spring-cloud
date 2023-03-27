package com.example.streamservice.controller;

import com.example.streamservice.component.CustomSourceBean;
import com.example.streamservice.component.SimpleSourceBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RestController
public class MessageSendController {

    @Autowired
    private SimpleSourceBean simpleSourceBean;

    @Autowired
    private CustomSourceBean customSourceBean;

    /**
     * 删除操作
     * @param id
     * @return
     */
    @DeleteMapping("/delete/{id}")
    public HashMap<String,Object> delOperation(@PathVariable Long id){
        simpleSourceBean.PublishMessage("add",id);
        customSourceBean.publishMessage("add",id);
        HashMap<String, Object> map = new HashMap<>();
        map.put("msg","成功");
        map.put("code",200);
        return map;
    }


    /**
     * 新增操作
     * @param id
     * @return
     */
    @PostMapping("/add/{id}")
    public HashMap<String,Object> addOperation(@PathVariable Long id){
        simpleSourceBean.PublishMessage("add",id);
        customSourceBean.publishMessage("add",id);
        HashMap<String, Object> map = new HashMap<>();
        map.put("msg","成功");
        map.put("code",200);
        return map;
    }

    /**
     * 更新操作
     * @param id
     * @return
     */
    @GetMapping("/update")
    public HashMap<String,Object> updateOperation(@RequestParam Long id){
        simpleSourceBean.PublishMessage("update",id);
        customSourceBean.publishMessage("update",id);
        HashMap<String, Object> map = new HashMap<>();
        map.put("msg","成功");
        map.put("code",200);
        return map;
    }
}
