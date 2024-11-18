package com.example.Tbank_fj_2024_COURSE_PROJECT;

import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.handlers.CallbackHandler;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.handlers.CommandHandler;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.UserStateEnum;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.MessageSender;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.SessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class MovieBot extends TelegramLongPollingBot {

    private static final Logger logger = LoggerFactory.getLogger(MovieBot.class);

    private final CommandHandler commandHandler;
    private final CallbackHandler callbackHandler;
    private final SessionService sessionService;
    private final MessageSender messageSender;

    @Autowired
    public MovieBot(CommandHandler commandHandler, CallbackHandler callbackHandler, SessionService sessionService, MessageSender messageSender) {
        this.commandHandler = commandHandler;
        this.callbackHandler = callbackHandler;
        this.sessionService = sessionService;
        this.messageSender = messageSender;
    }

    private void handleTextMessage(String chatId, String messageText) {
        SessionService.UserState userState = sessionService.getUserState(chatId);
        if (userState != null) {
            commandHandler.handleStateBasedCommand(chatId, messageText, userState.getState());
        } else {
            // Если состояние не определено, отправить пользователя в начальное состояние
            sessionService.setUserState(chatId, UserStateEnum.DEFAULT_UNLOGGED);
            messageSender.sendMessage(chatId, "Вы не авторизованы. Пожалуйста, используйте /login или /register.");
        }
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
/*
    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {

    }



    @Override
    public String getBotPath() {
        return "/webhook";
    }

 */

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            String chatId = update.getMessage().getChatId().toString();
            logger.info("Received message from chatId: {}, text: {}", chatId, messageText);

            handleTextMessage(chatId, messageText);

        } else if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            String chatId = update.getCallbackQuery().getMessage().getChatId().toString();

            // Разделяем callbackData на префикс и данные после префикса
            String[] parts = callbackData.split(":", 2);
            String action = parts[0]; // префикс (например, select_movie, accept_request и т.д.)
            String data = (parts.length > 1) ? parts[1] : ""; // данные после префикса

            switch (action) {
                case "select_movie":
                    sessionService.setContext(chatId, data); // обрабатываем данные после select_movie:
                    callbackData = "select_movie"; // устанавливаем callbackData для логирования и обработки
                    break;
                case "accept_request":
                    sessionService.setContext(chatId, data);
                    break;
                case "reject_request":
                    sessionService.setContext(chatId, data);
                    break;
                case "cancel_request":
                    sessionService.setContext(chatId, data);
                    break;
                default:
                    logger.warn("Received unsupported callback action: {}", action);
                    break;
            }

            logger.info("Received callback from chatId: {}, action: {}, data: {}", chatId, action, data);

            // Вызываем обработчик для callback
            callbackHandler.handleCallbackQuery(chatId, action);
        } else {
            logger.warn("Received unsupported update type: {}", update);
        }
    }
}