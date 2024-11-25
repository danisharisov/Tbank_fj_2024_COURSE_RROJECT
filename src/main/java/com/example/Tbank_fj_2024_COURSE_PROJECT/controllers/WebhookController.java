package com.example.Tbank_fj_2024_COURSE_PROJECT.controllers;

/*
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.telegram.telegrambots.meta.api.objects.Update;

@RestController
public class WebhookController {

    private final RabbitTemplate rabbitTemplate;
    private final MovieBot movieBot;
    private final String secretToken;

    public WebhookController(MovieBot movieBot,
                             RabbitTemplate rabbitTemplate,
                             @Value("${telegram.bot.secret.token}") String secretToken) {
        this.movieBot = movieBot;
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

        // Отправляем обновление в RabbitMQ
        rabbitTemplate.convertAndSend("webhook-queue", update);

        return ResponseEntity.ok("Webhook received");
    }
}


 */