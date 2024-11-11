package com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services;

import com.example.Tbank_fj_2024_COURSE_PROJECT.models.movie.Movie;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.user.AppUser;
import com.example.Tbank_fj_2024_COURSE_PROJECT.services.MovieService;
import com.example.Tbank_fj_2024_COURSE_PROJECT.services.SessionService;
import com.example.Tbank_fj_2024_COURSE_PROJECT.services.UserMovieService;
import com.example.Tbank_fj_2024_COURSE_PROJECT.services.UserStateEnum;
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


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Component
public class MessageSender {

    private static final Logger logger = LoggerFactory.getLogger(MessageSender.class);

    private final TelegramLongPollingBot bot;
    private final SessionService sessionService;
    private final UserMovieService userMovieService;

    @Autowired
    public MessageSender(@Lazy TelegramLongPollingBot bot, @Lazy SessionService sessionService, @Lazy UserMovieService userMovieService) {
        this.bot = bot;
        this.sessionService = sessionService;
        this.userMovieService = userMovieService;
    }

    public void sendMainMenu(String chatId) {
        sessionService.clearUserState(chatId);
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Выберите действие из меню ниже:");

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        // Создание кнопок для основного меню
        List<InlineKeyboardButton> rowInline1 = new ArrayList<>();
        rowInline1.add(InlineKeyboardButton.builder()
                .text("Добавить фильм")
                .callbackData("add_movie")
                .build());
        rowsInline.add(rowInline1);

        List<InlineKeyboardButton> rowInline2 = new ArrayList<>();
        rowInline2.add(InlineKeyboardButton.builder()
                .text("Просмотренные фильмы")
                .callbackData("view_watched_movies")
                .build());
        rowsInline.add(rowInline2);

        List<InlineKeyboardButton> rowInline3 = new ArrayList<>();
        rowInline3.add(InlineKeyboardButton.builder()
                .text("Запланированные фильмы")
                .callbackData("view_planned_movies")
                .build());
        rowsInline.add(rowInline3);

        List<InlineKeyboardButton> rowInline4 = new ArrayList<>();
        rowInline4.add(InlineKeyboardButton.builder()
                .text("Друзья")
                .callbackData("friends_menu")
                .build());
        rowsInline.add(rowInline4);

        // Установка клавиатуры
        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);

        try {
            bot.execute(message);
            logger.info("Main menu sent to chatId: {}", chatId);
        } catch (TelegramApiException e) {
            logger.error("Error sending main menu to chatId: {}", chatId, e);
        }
    }




    public void sendMessage(String chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        try {
            bot.execute(message);
            logger.info("Message sent to chatId: {}", chatId);
        } catch (TelegramApiException e) {
            logger.error("Error sending message to chatId: {}", chatId, e);
        }
    }

    public void sendWatchedMovies(String chatId, List<Movie> watchedMovies) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);

        StringBuilder sb = new StringBuilder("Ваши просмотренные фильмы:\n");
        for (int i = 0; i < watchedMovies.size(); i++) {
            Movie movie = watchedMovies.get(i);
            sb.append(i + 1).append(". ").append(movie.getTitle()).append(" (").append(movie.getYear()).append(")\n");
        }
        message.setText(sb.toString());

        // Создаем кнопки удаления
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        for (Movie movie : watchedMovies) {
            List<InlineKeyboardButton> rowInline = new ArrayList<>();
            InlineKeyboardButton deleteButton = new InlineKeyboardButton("Удалить: " + movie.getTitle());
            deleteButton.setCallbackData("remove_watched:" + movie.getImdbId());
            rowInline.add(deleteButton);
            rowsInline.add(rowInline);
        }

        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);

        try {
            bot.execute(message);
            logger.info("Watched movies message sent to chatId: {}", chatId);
        } catch (TelegramApiException e) {
            logger.error("Error sending watched movies to chatId: {}", chatId, e);
        }
    }




    public void sendSimpleMovieList(String chatId, List<Movie> movies) {
        logger.info(sessionService.getUserState(chatId) + "Мы заходим в sendSimpleMovieList");
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        for (Movie movie : movies) {
            InlineKeyboardButton movieButton = InlineKeyboardButton.builder()
                    .text(movie.getTitle() + " (" + movie.getYear() + ")")
                    .callbackData("select_movie:" + movie.getImdbId())
                    .build();
            List<InlineKeyboardButton> rowInline = new ArrayList<>();
            rowInline.add(movieButton);
            rowsInline.add(rowInline);
        }

        markupInline.setKeyboard(rowsInline);
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text("Выберите фильм для добавления:")
                .replyMarkup(markupInline)
                .build();

        try {
            bot.execute(message);
            logger.info("Simple movie list sent to chatId: {}", chatId);
        } catch (TelegramApiException e) {
            logger.error("Error sending simple movie list to chatId: {}", chatId, e);
        }
        logger.info(sessionService.getUserState(chatId) + "Мы  в конче sendSimpleMovieList");
    }


    public void sendMovieDetails(String chatId, Movie movie, Double userRating, double averageFriendRating) {
        String message = String.format(
                "Название: %s\nГод: %s\nМоя оценка: %s\nСредняя оценка среди друзей: %.2f\nОценка IMDb: %s",
                movie.getTitle(),
                movie.getYear(),
                (userRating != null ? userRating.toString() : "Не оценено"),
                averageFriendRating,
                movie.getImdbRating()
        );

        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        buttons.add(
                Arrays.asList(
                        InlineKeyboardButton.builder()
                                .text("Удалить фильм")
                                .callbackData("delete_movie")
                                .build(),
                        InlineKeyboardButton.builder()
                                .text("Добавить оценку")
                                .callbackData("rate_movie")
                                .build(),
                        InlineKeyboardButton.builder()
                                .text("Главное меню")
                                .callbackData("main_menu")
                                .build()
                )
        );

        sendMessageWithInlineKeyboard(chatId, message, buttons);
    }

    public void sendPlannedMovieDetails(String chatId, AppUser user, Movie movie, int userHype, double averageFriendHype) {
        String message = String.format(
                "Название: %s\nГод: %s\nМой ажиотаж: %d\nАжиотаж среди друзей: %.2f\nОценка IMDb: %s",
                movie.getTitle(),
                movie.getYear(),
                userHype,
                averageFriendHype,
                movie.getImdbRating()
        );

        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        buttons.add(
                Arrays.asList(
                        InlineKeyboardButton.builder()
                                .text("Удалить фильм")
                                .callbackData("delete_planned")
                                .build(),
                        InlineKeyboardButton.builder()
                                .text("Добавить ажиотаж")
                                .callbackData("add_hype")
                                .build(),
                        InlineKeyboardButton.builder()
                                .text("Главное меню")
                                .callbackData("main_menu")
                                .build()
                )
        );

        sendMessageWithInlineKeyboard(chatId, message, buttons);
    }

    public void showWatchedMoviesList(String chatId, List<Movie> watchedMovies) {
        StringBuilder response = new StringBuilder("Ваши просмотренные фильмы:\n");
        for (int i = 0; i < watchedMovies.size(); i++) {
            response.append(i + 1).append(". ").append(watchedMovies.get(i).getTitle()).append(" (").append(watchedMovies.get(i).getYear()).append(")\n");
        }
        sendMessage(chatId, response.toString());
        sessionService.setUserState(chatId, UserStateEnum.AWAITING_MOVIE_SELECTION_USER);
        sendMessage(chatId, "Введите номер фильма, чтобы просмотреть его детали:");
    }

    public void sendMessageWithInlineKeyboard(String chatId, String text, List<List<InlineKeyboardButton>> buttons) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(buttons);
        message.setReplyMarkup(inlineKeyboardMarkup);

        try {
            bot.execute(message);
        } catch (TelegramApiException e) {
            logger.error("Ошибка при отправке сообщения с inline-клавиатурой: ", e);
        }
    }



    public void processAddMovieStatusSelection(String chatId) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        InlineKeyboardButton plannedButton = InlineKeyboardButton.builder()
                .text("Запланировать")
                .callbackData("select_status:planned")
                .build();
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(plannedButton);

        InlineKeyboardButton watchedButton = InlineKeyboardButton.builder()
                .text("Просмотрен")
                .callbackData("select_status:watched")
                .build();
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(watchedButton);

        rowsInline.add(row1);
        rowsInline.add(row2);

        markupInline.setKeyboard(rowsInline);
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text("Выберите статус для добавления фильма:")
                .replyMarkup(markupInline)
                .build();

        try {
            bot.execute(message);
            logger.info("Message with status selection sent to chatId: {}", chatId);
        } catch (TelegramApiException e) {
            logger.error("Error sending status selection message to chatId: {}", chatId, e);
        }
    }






    public void sendFriendRequestsMenu(String chatId, List<AppUser> friendRequests, boolean isIncoming) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);

        // В зависимости от параметра isIncoming, формируем разные заголовки
        if (isIncoming) {
            message.setText("Ваши входящие запросы в друзья:");
        } else {
            message.setText("Ваши исходящие запросы в друзья:");
        }

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        for (AppUser request : friendRequests) {
            List<InlineKeyboardButton> rowInline = new ArrayList<>();
            InlineKeyboardButton acceptButton = new InlineKeyboardButton();
            acceptButton.setText("Принять: " + request.getUsername());
            acceptButton.setCallbackData("accept_request:" + request.getUsername());

            if (isIncoming) {
                InlineKeyboardButton rejectButton = new InlineKeyboardButton();
                rejectButton.setText("Отклонить: " + request.getUsername());
                rejectButton.setCallbackData("reject_request:" + request.getUsername());
                rowInline.add(acceptButton);
                rowInline.add(rejectButton);
            } else {
                // Для исходящих запросов добавляем кнопку для отмены
                InlineKeyboardButton cancelButton = new InlineKeyboardButton();
                cancelButton.setText("Отменить: " + request.getUsername());
                cancelButton.setCallbackData("cancel_request:" + request.getUsername());
                rowInline.add(cancelButton);
            }

            rowsInline.add(rowInline);
        }

        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);

        try {
            bot.execute(message);
            logger.info("Friend requests menu sent to chatId: {}", chatId);
        } catch (TelegramApiException e) {
            logger.error("Error sending friend requests menu to chatId: {}", chatId, e);
        }
    }

    public void sendFriendsMenu(String chatId, List<AppUser> friends) {
        StringBuilder response = new StringBuilder();

        // Проверка наличия друзей и формирование сообщения
        if (friends.isEmpty()) {
            response.append("У вас нет друзей.\n");
        } else {
            response.append("Ваши друзья:\n");
            for (int i = 0; i < friends.size(); i++) {
                response.append(i + 1).append(". ").append(friends.get(i).getUsername()).append("\n");
            }
        }

        // Создание кнопок для меню
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        // Кнопка для удаления друга
        if (!friends.isEmpty()) {
            List<InlineKeyboardButton> deleteFriendButton = new ArrayList<>();
            deleteFriendButton.add(InlineKeyboardButton.builder()
                    .text("Удалить друга")
                    .callbackData("delete_friend")
                    .build());
            rowsInline.add(deleteFriendButton);
        }

        // Кнопка для отправки заявки
        List<InlineKeyboardButton> requestRow = new ArrayList<>();
        requestRow.add(InlineKeyboardButton.builder()
                .text("Отправить запрос в друзья")
                .callbackData("send_friend_request")
                .build());
        rowsInline.add(requestRow);

        // Кнопки для работы с заявками
        List<InlineKeyboardButton> rowInline1 = new ArrayList<>();
        rowInline1.add(InlineKeyboardButton.builder()
                .text("Входящие заявки")
                .callbackData("incoming_requests")
                .build());
        rowInline1.add(InlineKeyboardButton.builder()
                .text("Исходящие заявки")
                .callbackData("outgoing_requests")
                .build());
        rowsInline.add(rowInline1);

        // Кнопка возврата в главное меню
        List<InlineKeyboardButton> rowInline2 = new ArrayList<>();
        rowInline2.add(InlineKeyboardButton.builder()
                .text("Главное меню")
                .callbackData("main_menu")
                .build());
        rowsInline.add(rowInline2);

        // Присваиваем кнопки сообщению
        markupInline.setKeyboard(rowsInline);

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(response.toString());
        message.setReplyMarkup(markupInline);

        try {
            bot.execute(message);
            logger.info("Friends menu sent to chatId: {}", chatId);
        } catch (TelegramApiException e) {
            logger.error("Error sending friends menu to chatId: {}", chatId, e);
        }
    }



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
                            .callbackData("delete_planned:" + movie.getImdbId())
                            .build()
            );
        }
        actionButtons.add(
                InlineKeyboardButton.builder()
                        .text("Добавить ажиотаж")
                        .callbackData("add_hype:" + movie.getImdbId())
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
    }



}
