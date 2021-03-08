package com.example.queue.topic;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * TODO
 *
 * @author admin
 * @version 1.0
 * @date 2021/2/3 14:25
 */
@RestController
public class TestController {
    @Autowired
    RabbitTemplate template;

    @GetMapping("/topic1")
    public String topic1(){
        template.convertAndSend("exchange","topic.message","hello,rabbit~~~11");
        return "topic1";
    }
    @GetMapping("/topic2")
    public String topic2(){
        template.convertAndSend("exchange","topic.messages","hello,rabbit~~~22");
        return "topic2";
    }

}
