package com.example.queue.dynamicRabbitmq;

import org.springframework.amqp.rabbit.annotation.RabbitListener;

/**
 * TODO
 *
 * @author admin
 * @version 1.0
 * @date 2021/2/19 11:06
 */
public class reciveMessage {
    @RabbitListener   //监听器监听指定的队列Queue
    public void processC(String str,String queueName) {
        System.out.println("queue:"+queueName);
        System.out.println("Receive:"+str);
    }
}
