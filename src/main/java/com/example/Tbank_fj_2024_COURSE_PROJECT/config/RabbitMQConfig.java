package com.example.Tbank_fj_2024_COURSE_PROJECT.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String QUEUE_NAME = "telegram_updates_queue";

    @Bean
    public Queue telegramUpdatesQueue() {
        return new Queue(QUEUE_NAME, true);
    }
}
