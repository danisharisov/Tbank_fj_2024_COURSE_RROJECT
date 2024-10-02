package com.example.Tbank_fj_2024_COURSE_RROJECT.telegram;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
public class MovieBotConfig {

    private static final Logger logger = LoggerFactory.getLogger(MovieBotConfig.class);

    @Autowired
    private MovieBot movieBot;

    @Bean
    public TelegramBotsApi telegramBotsApi() {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(movieBot);
            return botsApi;
        } catch (TelegramApiException e) {
            logger.error("Failed to register bot", e);
            throw new RuntimeException("Failed to register bot", e);
        }
    }
}