package com.example.messagereceiveservice.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;

import java.util.HashMap;

@EnableBinding(Sink.class)
public class InputChannelListener {

    private static final Logger logger = LoggerFactory.getLogger(InputChannelListener.class);

    /**
     * 接收input通道的消息
     * @param hashMap
     */
    @StreamListener(Sink.INPUT)
    public void loggerSink(HashMap<String,Object> hashMap){
        logger.info("接受的内容为： {}", hashMap);
    }
}
