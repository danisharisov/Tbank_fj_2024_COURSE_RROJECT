package com.example.Tbank_fj_2024_COURSE_PROJECT.telegram;

import com.example.Tbank_fj_2024_COURSE_PROJECT.models.user.AppUser;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.handlers.CallbackHandler;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.handlers.CommandHandler;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.handlers.UnloggedStateHandler;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.UserStateEnum;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.MessageSender;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.SessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Component
public class MovieBot extends TelegramWebhookBot {

    private static final Logger logger = LoggerFactory.getLogger(MovieBot.class);

    private final CommandHandler commandHandler;
    private final CallbackHandler callbackHandler;
    private final SessionService sessionService;
    private final MessageSender messageSender;
    private final UnloggedStateHandler unloggedStateHandler;

    @Autowired
    public MovieBot(CommandHandler commandHandler, CallbackHandler callbackHandler,
                    SessionService sessionService, MessageSender messageSender,
                    UnloggedStateHandler unloggedStateHandler) {
        this.commandHandler = commandHandler;
        this.callbackHandler = callbackHandler;
        this.sessionService = sessionService;
        this.messageSender = messageSender;
        this.unloggedStateHandler = unloggedStateHandler;
    }

    private void handleTextMessage(String chatId, String messageText, String username) {
        SessionService.UserState userState = sessionService.getUserState(chatId);
        AppUser currentUser = sessionService.getCurrentUser(chatId);

        if (currentUser == null) {
            if ("/start".equals(messageText)) {
                unloggedStateHandler.handleUnloggedState(chatId, messageText, username);
            } else {
                messageSender.sendMessage(chatId, "Вы еще не авторизованы. Нажмите /start для начала работы.");
            }
            return;
        }

        if (userState != null) {
            commandHandler.handleStateBasedCommand(chatId, messageText, userState.getState(), username);
        } else {
            sessionService.setUserState(chatId, UserStateEnum.DEFAULT_LOGGED);
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

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            String chatId = update.getMessage().getChatId().toString();
            String username = update.getMessage().getFrom().getUserName();

            logger.info("Received message from chatId: {}, text: {}, username: {}", chatId, messageText, username);
            handleTextMessage(chatId, messageText, username);

        } else if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            String chatId = update.getCallbackQuery().getMessage().getChatId().toString();
            String[] parts = callbackData.split(":", 2);
            String action = parts[0]; // префикс (например, select_movie, accept_request и т.д.)
            String data = (parts.length > 1) ? parts[1] : ""; // данные после префикса
            sessionService.setContext(chatId, data);
            logger.info("Received callback from chatId: {}, action: {}, data: {}", chatId, action, data);

            callbackHandler.handleCallbackQuery(chatId, action);
        } else {
            logger.warn("Received unsupported update type: {}", update);
        }
        return null;
    }

    public void sendPhotoWithInlineKeyboard(String chatId, String photoUrl, String caption, List<List<InlineKeyboardButton>> buttons) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(chatId);
        sendPhoto.setPhoto(new InputFile(photoUrl));
        sendPhoto.setCaption(caption);
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(buttons);
        sendPhoto.setReplyMarkup(inlineKeyboardMarkup);
        try {
            execute(sendPhoto);
            logger.info("Photo with caption sent to chatId: {}", chatId);
        } catch (TelegramApiException e) {
            logger.error("Ошибка при отправке фото: {}", e.getMessage());
        }
    }

    @Override
    public String getBotPath() {
        return "/webhook";
    }

}