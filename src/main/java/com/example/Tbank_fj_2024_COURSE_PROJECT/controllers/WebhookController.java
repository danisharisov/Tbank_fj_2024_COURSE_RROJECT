package com.example.Tbank_fj_2024_COURSE_PROJECT.controllers;

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

    private final RabbitTemplate rabbitTemplate;
    private final String secretToken;

    public WebhookController(RabbitTemplate rabbitTemplate,
                             @Value("${telegram.bot.secret.token}") String secretToken) {
        this.rabbitTemplate = rabbitTemplate;
        this.secretToken = secretToken;
    }

    @PostMapping("/webhook")
    public ResponseEntity<?> onUpdateReceived(
            @RequestHeader("X-Telegram-Bot-Api-Secret-Token") String receivedToken,
            @RequestBody Update update) {

        if (!receivedToken.equals(secretToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid Secret Token");
        }

        rabbitTemplate.convertAndSend("telegram_updates_queue", update);
        return ResponseEntity.ok("Webhook received and sent to RabbitMQ");
    }
}
