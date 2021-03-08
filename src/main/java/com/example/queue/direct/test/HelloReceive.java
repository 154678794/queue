package com.example.queue.direct.test;

import com.example.queue.direct.User;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * TODO
 * 监听队列
 * @author admin
 * @version 1.0
 * @date 2021/2/3 13:53
 */
@Component
public class HelloReceive {
    @RabbitListener(queues="queue")    //监听器监听指定的队列Queue
    public void processC(String str) {
        System.out.println("Receive:"+str);
    }
    @RabbitListener(queues="queueUser")    //监听器监听指定的QueueUser
    public void processUser(User user) {
        System.out.println("Receive:"+user);
    }

}