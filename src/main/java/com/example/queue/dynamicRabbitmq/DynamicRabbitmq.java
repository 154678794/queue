package com.example.queue.dynamicRabbitmq;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.DirectMessageListenerContainer;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 动态MQ
 * 实现动态添加队列及绑定关系、动态添加监听
 * 动态调整监听线程池大小
 * 动态删除队列、动态取消监听
 * 发送动态队列的消息
 *
 * @author tiancong
 * @date 2020/11/26 15:55
 */
@Component
@Data
@Slf4j
public class DynamicRabbitmq {
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    @Lazy
    private RabbitAdmin rabbitAdmin;

    /**
     * 动态交换机默认名称
     */
    private static final String EXCHANGE = "yd.dynamic.exchange";

    /**
     * 默认死信交换机名称
     */
    private static final String DLX_EXCHANGE = "yd.dynamic.exchange.dlx";

    /**
     * 队列前缀
     */
    private static final String QUEUE_PREFIX = "yd.dynamic.queue.";

    /**
     * 默认死信队列名称
     */
    private static final String DLX_QUEUE = "yd.dynamic.queue.dlx";

    /**
     * 默认死信队列名称
     */
    private static final String DLX_ROUTING = "yd.dynamic.routing.dlx";

    private static final Map<String, DirectMessageListenerContainer> CONTAINER_MAP = new ConcurrentHashMap<>(8);

    /**
     * 动态添加队列及绑定关系
     *
     * @param queueName  队列名
     * @param exchange   交换机名
     * @param routingKey 路由名
     * @param needDlx    需要死信队列
     */
    public void addQueueAndExchange(String queueName, String exchange, String routingKey, boolean needDlx) {
        queueName = getFullQueueName(queueName);
        Queue queue = new Queue(queueName);
        if (needDlx) {
            Map<String, Object> arguments = new HashMap<>(2);
            arguments.put("x-dead-letter-exchange", DLX_EXCHANGE);
            arguments.put("x-dead-letter-routing-key", DLX_ROUTING);
            queue = new Queue(queueName, true, false, false, arguments);
            QueueInformation queueInfo = rabbitAdmin.getQueueInfo(DLX_QUEUE);
            if (queueInfo == null) {
                Queue dlxQueue = new Queue(DLX_QUEUE);
                DirectExchange dlxDirectExchange = new DirectExchange(DLX_EXCHANGE);
                rabbitAdmin.declareQueue(dlxQueue);
                rabbitAdmin.declareExchange(dlxDirectExchange);
                rabbitAdmin.declareBinding(BindingBuilder.bind(dlxQueue).to(dlxDirectExchange).with(DLX_ROUTING));
                log.info("创建死信队[{}]列成功", DLX_QUEUE);
            }
        }
        TopicExchange topicExchange = new TopicExchange(exchange);
        rabbitAdmin.declareQueue(queue);
        rabbitAdmin.declareExchange(topicExchange);
        rabbitAdmin.declareBinding(BindingBuilder.bind(queue).to(topicExchange).with(routingKey));
    }

    /**
     * 动态删除队列（队列有消息时不删除）
     *
     * @param queueName 队列名
     */
    public void deleteQueue(String queueName) {
        queueName = getFullQueueName(queueName);
        if (Objects.requireNonNull(rabbitAdmin.getQueueInfo(queueName)).getMessageCount() == 0) {
            rabbitAdmin.deleteQueue(queueName);
            log.info("成功删除mq队列{}", queueName);
        } else {
            log.info("mq队列[{}]里还有消息。不做删除操作", queueName);
        }
    }


    /**
     * 动态添加队列监听
     *
     * @param queueName  队列名
     * @param routingKey 路由名
     */
    public void startListener(String queueName, String routingKey) {
        startListener(queueName, routingKey, 1, false);
    }

    /**
     * 动态添加队列监听及修改消费者线程池大小
     *
     * @param queueName   队列名
     * @param routingKey  路由名
     * @param consumerNum 消费者线程数量
     * @param needDlx     需要死信
     *
     *                    队列
     */
    public void startListener(String queueName, String routingKey, int consumerNum, boolean needDlx) {
        String FullqueueName = getFullQueueName(queueName);
        addQueueAndExchange(FullqueueName, EXCHANGE, routingKey, needDlx);
        DirectMessageListenerContainer container = new DirectMessageListenerContainer(rabbitTemplate.getConnectionFactory());
        DirectMessageListenerContainer getContainer = CONTAINER_MAP.putIfAbsent(FullqueueName, container);
        if (getContainer != null) {
            log.info("动态修改mq监听成功,交换机:{},路由key:{},队列:{},线程数:{}", EXCHANGE, routingKey, FullqueueName, consumerNum);
            container = getContainer;
        } else {
            container.setQueueNames(FullqueueName);
            log.info("动态添加mq监听成功,交换机:{},路由key:{},队列:{},线程数:{}", EXCHANGE, routingKey, FullqueueName, consumerNum);
        }
        container.setPrefetchCount(consumerNum);
        if (needDlx) {
            container.setAcknowledgeMode(AcknowledgeMode.AUTO);
        } else {
            container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        }
        container.setConsumersPerQueue(consumerNum);
        container.setMessageListener(new ConsumerHandler(!needDlx,queueName));
        container.setAdviceChain(createRetry());
        container.setDefaultRequeueRejected(false);
        container.start();
    }

    /**
     * 动态停止监听并删除队列
     *
     * @param queueName 队列名
     */
    public void stopListener(String queueName) {
        queueName = getFullQueueName(queueName);
        DirectMessageListenerContainer container = CONTAINER_MAP.get(queueName);
        if (container != null) {
            container.stop();
            container.destroy();
            CONTAINER_MAP.remove(queueName);
        }
        log.info("停止监听mq队列{}", queueName);
        deleteQueue(queueName);
    }

    /**
     * 发送动态队列的消息
     *
     * @param routingKey 路由名
     * @param data       数据
     */
    public void sendMsg(String routingKey, String data) {
        rabbitTemplate.convertAndSend(EXCHANGE, routingKey, data);
    }


    /**
     * 获取队列名全称
     *
     * @param queueName 队列名
     * @return 全称
     */
    private String getFullQueueName(String queueName) {
        if (queueName.startsWith(QUEUE_PREFIX)) {
            return queueName;
        }
        return QUEUE_PREFIX + queueName;
    }

    /**
     * 重试机制
     *
     * @return
     */
    private RetryOperationsInterceptor createRetry() {
        return RetryInterceptorBuilder
                .stateless()
                //重试次数
                .maxAttempts(3)
                //重试间隔  指数递增时间参数   最大间隔时间
                .backOffOptions(1000, 3, 5000)
                //次数用完之后的处理,用的是默认处理类,失败消息会到死信
                .recoverer(new RejectAndDontRequeueRecoverer())
                .build();
    }
}
