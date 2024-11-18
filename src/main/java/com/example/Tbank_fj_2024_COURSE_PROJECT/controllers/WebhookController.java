package com.example.Tbank_fj_2024_COURSE_PROJECT.controllers;

import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.MovieBot;
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

    private final MovieBot movieBot;
    private final String secretToken;

    public WebhookController(MovieBot movieBot,
                             @Value("${telegram.bot.secret.token}") String secretToken) {
        this.movieBot = movieBot;
        this.secretToken = secretToken;
    }

    @PostMapping("/webhook")
    public ResponseEntity<?> onUpdateReceived(
            @RequestHeader("X-Telegram-Bot-Api-Secret-Token") String receivedToken,
            @RequestBody Update update) {
        {
            if (!receivedToken.equals(secretToken)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid Secret Token");
            }
            movieBot.onWebhookUpdateReceived(update);
            return ResponseEntity.ok("Webhook received");
        }
    }
}

