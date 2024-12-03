package com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.schedulers;

import com.example.Tbank_fj_2024_COURSE_PROJECT.services.AppUserService;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.MessageSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class WeeklyNotificationScheduler {

    private static final Logger logger = LoggerFactory.getLogger(WeeklyNotificationScheduler.class);

    private final AppUserService appUserService;
    private final MessageSender messageSender;

    public WeeklyNotificationScheduler(AppUserService appUserService, MessageSender messageSender) {
        this.appUserService = appUserService;
        this.messageSender = messageSender;
    }

    @Scheduled(cron = "0 0 20 ? * SUN") // Каждое воскресенье в 20:00
    public void sendWeeklyReminder() {
        logger.info("Starting weekly reminder task...");

        appUserService.findAllUsers().forEach(user -> {
            try {
                String chatId = user.getTelegramId();
                String message = "✨ Неделя подошла к концу! ✨\n\n" +
                        "Не забывайте добавлять просмотренные фильмы, выставлять оценки и планировать фильмы " +
                        "для совместного просмотра с друзьями! 📽️🍿";
                messageSender.sendMessage(chatId, message);
                logger.info("Message sent to user with chatId: {}", chatId);
            } catch (Exception e) {
                logger.error("Failed to send weekly reminder to user: {}", user.getTelegramId(), e);
            }
        });

        logger.info("Weekly reminder task completed.");
    }
}
