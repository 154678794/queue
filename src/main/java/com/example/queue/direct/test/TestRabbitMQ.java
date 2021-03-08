package com.example.queue.direct.test;

import com.example.queue.direct.HelloSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * TODO
 *
 * @author admin
 * @version 1.0
 * @date 2021/2/3 13:52
 */
@RestController
public class TestRabbitMQ {

    @Autowired
    private HelloSender helloSender;

    @GetMapping("/testRabbit")
    public String testRabbit() {
        helloSender.send();
        return "cc";
    }
    @GetMapping("/testUser")
    public String testUser() {
        helloSender.sendUser();
        return "User";
    }
}
