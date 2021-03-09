package com.example.queue.dynamicRabbitmq;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    public void dynamicCreate(@RequestParam String queueName,
                              @RequestParam String dynamic){
        dynamicRabbitmq.startListener(queueName,dynamic);
    }

    @GetMapping("/dynamicMessage")
    public void dynamicMessage(@RequestParam String dynamicMessage,
                               @RequestParam String dynamic){
        dynamicRabbitmq.sendMsg(dynamic,dynamicMessage);
    }

    @GetMapping("/deleteQueue")
    public void deleteQueue(@RequestParam String queueName){
        dynamicRabbitmq.deleteQueue(queueName);
    }

    @GetMapping("/stopListener")
    public void stopListener(@RequestParam String queueName){
        dynamicRabbitmq.stopListener(queueName);
    }

}
