package com.example.queue.direct;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * TODO
 *
 * @author admin
 * @version 1.0
 * @date 2021/2/3 13:50
 */
@Component
public class HelloSender {
    @Autowired
    private AmqpTemplate template;
    //队列名和消息绑定
    public void send() {
        template.convertAndSend("queue","hello,rabbit~");
    }
    //实现序列化
    public void sendUser() {
        User user = new User("cc","123");
        template.convertAndSend("queueUser",user);
    }
}
