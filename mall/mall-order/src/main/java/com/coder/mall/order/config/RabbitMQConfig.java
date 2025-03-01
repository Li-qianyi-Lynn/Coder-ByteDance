package com.coder.mall.order.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class RabbitMQConfig {

    // 队列名称
    private static final String ORDER_QUEUE = "order.queue";

    // 交换机名称
    private static final String ORDER_EXCHANGE = "order.exchange";

    // 路由键
    private static final String ORDER_ROUTING_KEY = "order.routing.key";

    @Bean
    public Queue orderQueue() {
        return new Queue(ORDER_QUEUE, true);
    }

    @Bean
    public DirectExchange orderExchange() {
        return new DirectExchange(ORDER_EXCHANGE);
    }

    @Bean
    public Binding orderBinding() {
        return BindingBuilder
                .bind(orderQueue())
                .to(orderExchange())
                .with(ORDER_ROUTING_KEY);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        
        // 消息发送到交换机确认
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                log.info("消息发送到交换机成功");
            } else {
                log.error("消息发送到交换机失败，原因: {}", cause);
            }
        });
        
        // 消息从交换机路由到队列确认
        rabbitTemplate.setReturnsCallback(returned -> {
            log.error("消息从交换机路由到队列失败: exchange: {}, route: {}, replyCode: {}, replyText: {}, message: {}",
                    returned.getExchange(), returned.getRoutingKey(), returned.getReplyCode(), 
                    returned.getReplyText(), returned.getMessage());
        });
        
        return rabbitTemplate;
    }
}
