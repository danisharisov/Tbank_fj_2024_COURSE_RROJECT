package com.example.Tbank_fj_2024_COURSE_PROJECT.rabbitmq;

import com.example.Tbank_fj_2024_COURSE_PROJECT.config.RabbitMQConfig;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.MovieBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
public class RabbitMQListener {
    private static final Logger logger = LoggerFactory.getLogger(RabbitMQListener.class);

    private final MovieBot movieBot;

    public RabbitMQListener(MovieBot movieBot) {
        this.movieBot = movieBot;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void receiveMessage(Update update) {
        try {
            logger.info("Получено сообщение от RabbitMQ: {}", update);
            movieBot.onWebhookUpdateReceived(update);
        } catch (Exception e) {
            logger.error("Ошибка при обработке сообщения: {}", update, e);
        }
    }
}
