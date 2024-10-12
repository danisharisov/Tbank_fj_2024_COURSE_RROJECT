package com.example.Tbank_fj_2024_COURSE_PROJECT.telegram;

import com.example.Tbank_fj_2024_COURSE_PROJECT.services.SessionService;
import com.example.Tbank_fj_2024_COURSE_PROJECT.services.UserStateEnum;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.handlers.CallbackHandler;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.handlers.CommandHandler;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.MessageSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

@Component
public class MovieBot extends TelegramLongPollingBot {

    private static final Logger logger = LoggerFactory.getLogger(MovieBot.class);

    private final CommandHandler commandHandler;
    private final CallbackHandler callbackHandler;
    private final SessionService sessionService; // Добавляем SessionService для управления состояниями
    private final MessageSender messageSender;

    @Autowired
    public MovieBot(CommandHandler commandHandler, CallbackHandler callbackHandler, SessionService sessionService, MessageSender messageSender) {
        this.commandHandler = commandHandler;
        this.callbackHandler = callbackHandler;
        this.sessionService = sessionService; // Инициализируем SessionService
        this.messageSender = messageSender;
    }

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
            logger.info("Received callback from chatId: {}, data: {}", chatId, callbackData);

            handleCallbackQuery(chatId, callbackData);
        } else {
            logger.warn("Received unsupported update type: {}", update);
        }
    }


    private void handleCallbackQuery(String chatId, String callbackData) {
        switch (callbackData) {
            case "add_movie":
                // Устанавливаем состояние ожидания для названия фильма
                sessionService.setUserState(chatId, UserStateEnum.WAITING_FOR_MOVIE_TITLE);
                messageSender.sendMessage(chatId, "Введите название фильма для добавления:");
                break;
            case "view_watched_movies":
                commandHandler.handleWatchedMoviesCommand(chatId);
                break;
            case "friends_menu":
                commandHandler.handleFriendsMenu(chatId);
                break;
            case "friend_requests_menu":
                commandHandler.handleIncomingRequestsCommand(chatId);
                break;
            default:
                callbackHandler.handleCallbackQuery(chatId, callbackData);
                break;
        }
    }



    /**
     * Метод для обработки текстовых сообщений в зависимости от текущего состояния пользователя.
     */
    private void handleTextMessage(String chatId, String messageText) {
        // Получаем объект UserState
        SessionService.UserState userState = sessionService.getUserState(chatId);

        // Извлекаем состояние из объекта UserState
        UserStateEnum state = userState.getState();

        switch (state) {
            case WAITING_FOR_MOVIE_TITLE:
                // Если ожидаем ввод названия фильма
                commandHandler.processAddMovie(chatId, messageText); // Вызываем метод processAddMovie
                sessionService.clearUserState(chatId); // Сбрасываем состояние после добавления фильма
                break;

            case WAITING_FOR_FRIEND_USERNAME:
                // Если ожидаем ввод имени друга
                commandHandler.processAddFriend(chatId, messageText); // Используем processAddFriend для обработки
                sessionService.clearUserState(chatId); // Сбрасываем состояние после добавления друга
                break;

            default:
                // Если состояние не ожидает ввода, передаем сообщение в CommandHandler
                commandHandler.handleMessage(chatId, messageText);
                break;
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
}
