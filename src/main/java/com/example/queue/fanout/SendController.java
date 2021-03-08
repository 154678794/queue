package com.example.queue.fanout;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * TODO
 *
 * @author admin
 * @version 1.0
 * @date 2021/2/3 14:46
 */
@RestController
public class SendController {
    @Autowired
    RabbitTemplate rabbitTemplate;

    @GetMapping("/sendAll")
    public String sendAll(){
        rabbitTemplate.convertAndSend("fanoutExchange","","xixi,haha");//参数2忽略
        return "sendAll";
    }
}
