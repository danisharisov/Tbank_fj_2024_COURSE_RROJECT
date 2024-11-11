package com.example.Tbank_fj_2024_COURSE_PROJECT.telegram;

import com.example.Tbank_fj_2024_COURSE_PROJECT.services.SessionService;
import com.example.Tbank_fj_2024_COURSE_PROJECT.services.UserStateEnum;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.handlers.CallbackHandler;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.handlers.CommandHandler;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.handlers.CommandParser;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.handlers.ParsedCommand;
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
    private final SessionService sessionService;
    private final MessageSender messageSender;

    @Autowired
    public MovieBot(CommandHandler commandHandler, CallbackHandler callbackHandler, SessionService sessionService, MessageSender messageSender) {
        this.commandHandler = commandHandler;
        this.callbackHandler = callbackHandler;
        this.sessionService = sessionService;
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
        if (callbackData.startsWith("cancel_request:")) {
            String friendUsername = callbackData.split(":")[1];
            commandHandler.handleCancelRequest(chatId, friendUsername);
        } else if (callbackData.startsWith("accept_request:")) {
            String friendUsername = callbackData.split(":")[1];
            commandHandler.handleAcceptRequest(chatId, friendUsername);
        } else if (callbackData.startsWith("reject_request:")) {
            String friendUsername = callbackData.split(":")[1];
            commandHandler.handleRejectRequest(chatId, friendUsername);
        } else if (callbackData.startsWith("select_status:")) {
            boolean isPlanned = callbackData.equals("select_status:planned");
            sessionService.setUserState(chatId, isPlanned ? UserStateEnum.WAITING_FOR_MOVIE_TITLE_PLANNED : UserStateEnum.WAITING_FOR_MOVIE_TITLE_WATCHED);
            messageSender.sendMessage(chatId, "Введите название фильма для добавления:");
        } else if (callbackData.startsWith("select_movie:")) {
            String imdbId = callbackData.split(":")[1];
            SessionService.UserState userState = sessionService.getUserState(chatId);
            if (userState != null) {
                UserStateEnum currentState = userState.getState();
                boolean isPlanned = (currentState == UserStateEnum.WAITING_FOR_MOVIE_TITLE_PLANNED);
                commandHandler.processMovieSelection(chatId, imdbId, isPlanned);
            } else {
                messageSender.sendMessage(chatId, "Ошибка: состояние пользователя не найдено.");
            }
        } else if (callbackData.startsWith("delete_planned:")) {
            String imdbId = callbackData.split(":")[1];
            commandHandler.handleDeletePlannedMovie(chatId, imdbId);
        } else if (callbackData.startsWith("add_hype:")) {
            String imdbId = callbackData.split(":")[1];
            commandHandler.handleSetHypeCommand(chatId, imdbId);
        } else {
            callbackHandler.handleCallbackQuery(chatId, callbackData);
        }
    }




    /**
     * Метод для обработки текстовых сообщений в зависимости от текущего состояния пользователя.
     */
    private void handleTextMessage(String chatId, String messageText) {
        // Получаем текущее состояние пользователя
        SessionService.UserState userState = sessionService.getUserState(chatId);

        if (userState != null) {
            // Обрабатываем команды в зависимости от состояния
            commandHandler.handleStateBasedCommand(chatId, messageText, userState.getState());
        } else {
            // Обрабатываем как обычную команду
            ParsedCommand parsedCommand = CommandParser.parse(messageText);
            commandHandler.handleUserCommand(chatId, parsedCommand.getCommandName(), parsedCommand.getArgs());
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
