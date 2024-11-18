package com.example.Tbank_fj_2024_COURSE_PROJECT.config;

import com.example.Tbank_fj_2024_COURSE_PROJECT.services.TelegramWebhookService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public CommandLineRunner run(TelegramWebhookService telegramWebhookService) {
        return args -> {
            telegramWebhookService.setWebhook();
        };
    }
}