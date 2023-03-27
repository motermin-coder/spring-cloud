package com.example.messagereceiveservice.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;

import java.util.HashMap;

@EnableBinding(CustomChannels.class)
public class CustomChannelListener {

    private static final Logger logger = LoggerFactory.getLogger(CustomChannelListener.class);

    @StreamListener("myChannelIn")
    public void listener(HashMap hashMap){
        logger.info("自定义通道myChannelIn接收的消息： {}", hashMap);
    }
}
