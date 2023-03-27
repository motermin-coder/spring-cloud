package com.example.securityservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.security.oauth2.client.test.OAuth2ContextConfiguration;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
@EnableAuthorizationServer
@EnableResourceServer
@RestController
@EnableEurekaClient
public class SecurityServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecurityServiceApplication.class, args);
    }

    /**
     * 该端点用于获取用户信息
     * @param authentication
     * @return
     */
    @GetMapping("/user")
    public Map<String,Object> user(OAuth2Authentication authentication){
        HashMap<String, Object> map = new HashMap<>();
        map.put("user",authentication.getUserAuthentication().getPrincipal());
        map.put("authorities",authentication.getUserAuthentication().getAuthorities());
        return map;
    }

}
