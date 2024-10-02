package com.example.Tbank_fj_2024_COURSE_RROJECT.telegram;

import com.example.Tbank_fj_2024_COURSE_RROJECT.telegram.handlers.CallbackHandler;
import com.example.Tbank_fj_2024_COURSE_RROJECT.telegram.handlers.CommandHandler;
import com.example.Tbank_fj_2024_COURSE_RROJECT.telegram.services.MessageSender;
import com.example.Tbank_fj_2024_COURSE_RROJECT.services.AppUserService;
import com.example.Tbank_fj_2024_COURSE_RROJECT.services.OmdbService;
import com.example.Tbank_fj_2024_COURSE_RROJECT.services.SessionService;
import com.example.Tbank_fj_2024_COURSE_RROJECT.services.TelegramAuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class MovieBot extends TelegramLongPollingBot {

    private static final Logger logger = LoggerFactory.getLogger(MovieBot.class);

    @Autowired
    private CommandHandler commandHandler;

    @Autowired
    private CallbackHandler callbackHandler;

    @Autowired
    private TelegramAuthService telegramAuthService;

    @Autowired
    private AppUserService appUserService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private OmdbService omdbService;

    @Autowired
    private MessageSender messageSender;

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            String chatId = update.getMessage().getChatId().toString();
            logger.info("Received message from chatId: {}, text: {}", chatId, messageText);
            handleMessage(chatId, messageText);
        } else if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            String chatId = update.getCallbackQuery().getMessage().getChatId().toString();
            callbackHandler.handleCallbackQuery(chatId, callbackData);
        }
    }

    private void handleMessage(String chatId, String messageText) {
        if (messageText.startsWith("/auth")) {
            commandHandler.handleAuthCommand(chatId, messageText);
        } else if (messageText.startsWith("/register")) {
            commandHandler.handleRegisterCommand(chatId, messageText);
        } else if (messageText.startsWith("/login")) {
            commandHandler.handleLoginCommand(chatId, messageText);
        } else if (messageText.startsWith("/addmovie")) {
            commandHandler.handleAddMovieCommand(chatId, messageText);
        } else if (messageText.startsWith("/watched")) {  // Изменено здесь
            commandHandler.handleWatchedMoviesCommand(chatId, messageText);
        } else {
            sendMessage(chatId, "Улыбок тебе дед макар : " + messageText);
        }
    }

    public void sendMessage(String chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        try {
            execute(message);
            logger.info("Message sent to chatId: {}", chatId);
        } catch (TelegramApiException e) {
            logger.error("Error sending message to chatId: {}", chatId, e);
        }
    }

    public TelegramAuthService getTelegramAuthService() {
        return telegramAuthService;
    }

    public AppUserService getAppUserService() {
        return appUserService;
    }

    public SessionService getSessionService() {
        return sessionService;
    }

    public OmdbService getOmdbService() {
        return omdbService;
    }

    public MessageSender getMessageSender() {
        return messageSender;
    }

    @Value("${telegram.bot.username}")
    private String botUsername;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }
}