package com.example.streamservice.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;

@Component
@EnableBinding(Source.class)
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
        source.output().send(MessageBuilder.withPayload(map).build());
    }
}
