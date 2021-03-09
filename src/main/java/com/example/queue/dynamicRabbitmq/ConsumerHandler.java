package com.example.queue.dynamicRabbitmq;

import com.rabbitmq.client.Channel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;

/**
 * TODO
 *
 * @author admin
 * @version 1.0
 * @date 2021/2/5 16:09
 */
@Slf4j
@Data
@AllArgsConstructor
public class ConsumerHandler implements ChannelAwareMessageListener {

    /**
     * 是否需要回应
     */
    private final Boolean needAck;

    private String queueName;

    /**
     * 接收消息
     *
     * @param message
     * @param channel
     * @throws Exception
     */
    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        int flag = (int) message.getMessageProperties().getHeaders().getOrDefault("retryCount", 0);
        flag++;
        if (flag > 1) {
            log.info("此消息第{}次执行", flag);
        }
        message.getMessageProperties().setHeader("retryCount", flag);
        String data = new String(message.getBody());
        log.info("[{}]收到mq消息: {}", message.getMessageProperties().getConsumerQueue(), data);
        if (getNeedAck()) {
            long deliveryTag = message.getMessageProperties().getDeliveryTag();
            try {
                handleMessage(data);
                channel.basicAck(deliveryTag, false);
            } catch (Exception e) {
                channel.basicNack(deliveryTag, false, true);
            }
        } else {
            handleMessage(data);
        }
    }

    /**
     * 处理消息
     *
     * @param data 消息体
     */
    public void handleMessage(String data) {

        new reciveMessage().processC(data,queueName);
    }
}
