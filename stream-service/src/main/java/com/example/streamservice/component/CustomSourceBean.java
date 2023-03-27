package com.example.streamservice.component;

import com.example.streamservice.event.CustomChannels;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;

@Component
@EnableBinding(CustomChannels.class)
public class CustomSourceBean {

    private CustomChannels customChannels;

    private static final Logger logger = LoggerFactory.getLogger(CustomSourceBean.class);

    @Autowired
    public CustomSourceBean(CustomChannels customChannels){
        this.customChannels = customChannels;
    }

    public void publishMessage(String action,Long id){
        logger.debug("发送消息到kafka: action = {}, id = {}",action,id);
        HashMap<String, Object> map = new HashMap<>();
        map.put("action",action);
        map.put("id",id);
        map.put("date",new Date());
        map.put("channel","myChannelOut");
        customChannels.myChannelOut().send(MessageBuilder.withPayload(map).build());
    }
}
