package com.example.queue.direct;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * TODO
 * 创建队列
 * @author admin
 * @version 1.0
 * @date 2021/2/3 13:47
 */
@Configuration
public class SenderConf {
    @Bean
    public Queue queue() {
        return new Queue("queue");
    }
    @Bean
    public Queue queueUser() {
        return new Queue("queueUser");
    }
}
