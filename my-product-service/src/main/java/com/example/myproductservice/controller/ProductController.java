package com.example.myproductservice.controller;

import com.example.myproductservice.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;

@RestController
public class ProductController {


    @Autowired
    private ProductService productService;

    @Value("${server.port}")
    private String port;


    @GetMapping("/list")
    public HashMap<String,Object> getProductList(HttpServletRequest request){
        System.out.println("tmx-correlation-id = " + request.getHeader("tmx-correlation-id"));
        HashMap<String, Object> map = new HashMap<>();
        List<String> strings = productService.ListProduct();
        map.put("list",strings);
        map.put("port",port);
        return map;
    }
}
