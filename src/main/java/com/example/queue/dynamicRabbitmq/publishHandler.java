package com.example.queue.dynamicRabbitmq;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * TODO
 *
 * @author admin
 * @version 1.0
 * @date 2021/2/19 10:06
 */
@RestController
public class publishHandler {

    @Autowired
    DynamicRabbitmq dynamicRabbitmq;

    @GetMapping("/dynamicCreate")
    public void dynamicCreate(){
        dynamicRabbitmq.startListener("dynamicQueue1","dynamic1");
    }

    @GetMapping("/dynamicMessage")
    public void dynamicMessage(){
        dynamicRabbitmq.sendMsg("dynamic1","dynamicMessage11");
    }

    @GetMapping("/deleteQueue")
    public void deleteQueue(){
        dynamicRabbitmq.deleteQueue("dynamicQueue1");
    }

    @GetMapping("/stopListener")
    public void stopListener(){
        dynamicRabbitmq.stopListener("dynamicQueue1");
    }

}
