package com.example.Tbank_fj_2024_COURSE_PROJECT.rabbitmq;

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

    @RabbitListener(queues = "telegram-queue")
    public void receiveMessage(Update update) {
        logger.info("Received message from RabbitMQ: {}", update);
        movieBot.processUpdate(update);
    }
}
