package com.example.Tbank_fj_2024_COURSE_PROJECT.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class TelegramWebhookService {

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${webhook.url}")
    private String webhookUrl;

    public void setWebhook() {
        String url = String.format("https://api.telegram.org/bot%s/setWebhook?url=%s", botToken, webhookUrl);
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getForObject(url, String.class);
    }

}