package com.example.Tbank_fj_2024_COURSE_PROJECT.controllers;

import com.example.Tbank_fj_2024_COURSE_PROJECT.services.RabbitMQSender;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.objects.Update;

@RestController
public class WebhookController {

    private final String secretToken;
    private final RabbitMQSender rabbitMQSender;

    public WebhookController(RabbitMQSender rabbitMQSender, @Value("${telegram.bot.secret.token}") String secretToken) {
        this.rabbitMQSender = rabbitMQSender;
        this.secretToken = secretToken;
    }

    @PostMapping("/webhook")
    public ResponseEntity<?> onUpdateReceived(
            @RequestHeader("X-Telegram-Bot-Api-Secret-Token") String receivedToken,
            @RequestBody Update update) {

        if (!receivedToken.equals(secretToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid Secret Token");
        }

        rabbitMQSender.send("telegram_updates_queue", update);
        return ResponseEntity.ok("Webhook received and sent to RabbitMQ");
    }
}
