package com.example.streamservice.event;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

public interface CustomChannels {

    @Output("myChannelOut")
    MessageChannel myChannelOut();
}
