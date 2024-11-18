package com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services;

import com.example.Tbank_fj_2024_COURSE_PROJECT.models.movie.Movie;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.user.AppUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.bots.TelegramWebhookBot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class MessageSender {

    private static final Logger logger = LoggerFactory.getLogger(MessageSender.class);

    private final TelegramLongPollingBot bot;
    private final SessionService sessionService;

    @Autowired
    public MessageSender(@Lazy TelegramLongPollingBot bot, SessionService sessionService) {
        this.bot = bot;
        this.sessionService = sessionService;
    }

    // Основное меню
    public void sendMainMenu(String chatId) {
        sessionService.clearUserState(chatId);
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Выберите действие из меню ниже:");

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        // Кнопки основного меню
        rowsInline.add(createButtonRow("Добавить фильм", "add_movie"));
        rowsInline.add(createButtonRow("Просмотренные фильмы", "view_watched_movies"));
        rowsInline.add(createButtonRow("Запланированные фильмы", "view_planned_movies"));
        rowsInline.add(createButtonRow("Друзья", "friends_menu"));

        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);

        sendMessage(message);
    }

    // Отправка сообщения без inline-кнопок
    public void sendMessage(String chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        sendMessage(message);
    }

    // Отправка списка просмотренных фильмов
    public void sendWatchedMovies(String chatId, List<Movie> watchedMovies) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);

        StringBuilder sb = new StringBuilder("Ваши просмотренные фильмы:\n");
        for (int i = 0; i < watchedMovies.size(); i++) {
            Movie movie = watchedMovies.get(i);
            sb.append(i + 1).append(". ").append(movie.getTitle()).append(" (").append(movie.getYear()).append(")\n");
        }
        message.setText(sb.toString());


        try {
            bot.execute(message);
            logger.info("Watched movies message sent to chatId: {}", chatId);
        } catch (TelegramApiException e) {
            logger.error("Error sending watched movies to chatId: {}", chatId, e);
        }
    }

    // Простое отображение списка фильмов для добавления
    public void sendSimpleMovieList(String chatId, List<Movie> movies) {
        if (movies.isEmpty()) {
            sendMessage(chatId, "Фильмы по вашему запросу не найдены.");
            return;
        }

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        for (Movie movie : movies) {
            rowsInline.add(createButtonRow(movie.getTitle() + " (" + movie.getYear() + ")", "select_movie:" + movie.getImdbId()));
        }

        markupInline.setKeyboard(rowsInline);

        SendMessage message = new SendMessage(chatId, "Выберите фильм для добавления:");
        message.setReplyMarkup(markupInline);

        sendMessage(message);
    }

    // Детали о фильме с оценками и кнопками действий
    public void sendMovieDetails(String chatId, Movie movie, Double userRating, double averageFriendRating) {

        String messageText = String.format(
                "Название: %s\nГод: %s\nМоя оценка: %s\nСредняя оценка среди друзей: %.2f\nОценка IMDb: %s",
                movie.getTitle(),
                movie.getYear(),
                (userRating != null ? userRating.toString() : "Не оценено"),
                averageFriendRating,
                movie.getImdbRating()
        );

        List<List<InlineKeyboardButton>> buttons = Arrays.asList(
                Arrays.asList(
                        createButton("Удалить фильм", "delete_movie"),
                        createButton("Добавить оценку", "rate_movie"),
                        createButton("Главное меню", "main_menu")
                )
        );

        sendMessageWithInlineKeyboard(chatId, messageText, buttons);
        sessionService.setSelectedMovie(chatId,movie);
    }

    // Детали о запланированном фильме с гипом и кнопками
    public void sendPlannedMovieDetailsWithOptions(String chatId, AppUser user, Movie movie, int userHype, double averageFriendHype, boolean isOwnMovie) {
        String message = String.format(
                "Название: %s\nГод: %s\nМой ажиотаж: %d\nАжиотаж среди друзей: %.2f\nОценка IMDb: %s",
                movie.getTitle(),
                movie.getYear(),
                userHype,
                averageFriendHype,
                movie.getImdbRating()
        );

        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        List<InlineKeyboardButton> actionButtons = new ArrayList<>();

        if (isOwnMovie) {
            actionButtons.add(
                    InlineKeyboardButton.builder()
                            .text("Удалить фильм")
                            .callbackData("delete_planned")
                            .build()
            );
        }
        actionButtons.add(
                InlineKeyboardButton.builder()
                        .text("Добавить ажиотаж")
                        .callbackData("add_hype")
                        .build()
        );
        actionButtons.add(
                InlineKeyboardButton.builder()
                        .text("Главное меню")
                        .callbackData("main_menu")
                        .build()
        );
        buttons.add(actionButtons);

        sendMessageWithInlineKeyboard(chatId, message, buttons);
        sessionService.setSelectedMovie(chatId,movie);
    }

    // Отправка inline-клавиатуры с выбором статуса фильма
    public void processAddMovieStatusSelection(String chatId) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        rowsInline.add(createButtonRow("Запланировать", "selected_planned"));
        rowsInline.add(createButtonRow("Просмотрен", "selected_watched"));
        sessionService.setUserState(chatId, UserStateEnum.WAITING_FOR_MOVIE_TITLE);
        markupInline.setKeyboard(rowsInline);
        SendMessage message = new SendMessage(chatId, "Выберите статус для добавления фильма:");
        message.setReplyMarkup(markupInline);

        sendMessage(message);
    }

    // Меню для входящих и исходящих запросов в друзья
    public void sendFriendRequestsMenu(String chatId, List<AppUser> friendRequests, boolean isIncoming) {
        String header = isIncoming ? "Ваши входящие запросы в друзья:" : "Ваши исходящие запросы в друзья:";
        SendMessage message = new SendMessage(chatId, header);

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        for (AppUser request : friendRequests) {
            List<InlineKeyboardButton> rowInline = new ArrayList<>();
            if (isIncoming) {
                rowInline.add(createButton("Принять: " + request.getUsername(), "accept_request:" + request.getUsername()));
                rowInline.add(createButton("Отклонить: " + request.getUsername(), "reject_request:" + request.getUsername()));
            } else {
                rowInline.add(createButton("Отменить: " + request.getUsername(), "cancel_request:" + request.getUsername()));
            }
            rowsInline.add(rowInline);
        }

        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);

        sendMessage(message);
    }

    // Меню друзей
    public void sendFriendsMenu(String chatId, List<AppUser> friends) {
        StringBuilder response = new StringBuilder("Ваши друзья:\n");
        for (int i = 0; i < friends.size(); i++) {
            response.append(i + 1).append(". ").append(friends.get(i).getUsername()).append("\n");
        }

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        if (!friends.isEmpty()) {
            rowsInline.add(createButtonRow("Удалить друга", "delete_friend"));
        }
        rowsInline.add(createButtonRow("Отправить запрос в друзья", "send_friend_request"));
        rowsInline.add(Arrays.asList(
                createButton("Входящие заявки", "incoming_requests"),
                createButton("Исходящие заявки", "outgoing_requests")
        ));
        rowsInline.add(createButtonRow("Главное меню", "main_menu"));

        markupInline.setKeyboard(rowsInline);
        SendMessage message = new SendMessage(chatId, response.toString());
        message.setReplyMarkup(markupInline);

        sendMessage(message);
    }

    // Утилиты для создания кнопок
    private List<InlineKeyboardButton> createButtonRow(String text, String callbackData) {
        return Arrays.asList(createButton(text, callbackData));
    }

    private InlineKeyboardButton createButton(String text, String callbackData) {
        return InlineKeyboardButton.builder().text(text).callbackData(callbackData).build();
    }

    private void sendMessage(SendMessage message) {
        try {
            bot.execute(message);
            logger.info("Message sent to chatId: {}", message.getChatId());
        } catch (TelegramApiException e) {
            logger.error("Error sending message to chatId: {}", message.getChatId(), e);
        }
    }

    private void sendMessageWithInlineKeyboard(String chatId, String text, List<List<InlineKeyboardButton>> buttons) {
        SendMessage message = new SendMessage(chatId, text);
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(buttons);
        message.setReplyMarkup(inlineKeyboardMarkup);
        sendMessage(message);
    }
}
