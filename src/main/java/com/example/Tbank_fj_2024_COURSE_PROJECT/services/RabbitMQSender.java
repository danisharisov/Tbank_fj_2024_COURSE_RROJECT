package com.example.Tbank_fj_2024_COURSE_PROJECT.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQSender {
    private static final Logger logger = LoggerFactory.getLogger(RabbitMQSender.class);
    private final RabbitTemplate rabbitTemplate;

    public RabbitMQSender(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void send(String queueName, Object message) {
        try {
            rabbitTemplate.convertAndSend(queueName, message);
            logger.info("Сообщение отправлено в очередь {}: {}", queueName, message);
        } catch (Exception e) {
            logger.error("Ошибка при отправке сообщения в очередь {}: {}", queueName, e.getMessage());
        }
    }
}
