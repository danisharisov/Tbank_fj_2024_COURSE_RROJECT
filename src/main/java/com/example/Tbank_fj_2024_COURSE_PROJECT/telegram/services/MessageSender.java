package com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services;

import com.example.Tbank_fj_2024_COURSE_PROJECT.models.movie.Movie;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.user.AppUser;
import com.example.Tbank_fj_2024_COURSE_PROJECT.services.SessionService;
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

    @Autowired
    public MessageSender(@Lazy TelegramLongPollingBot bot, @Lazy SessionService sessionService) {
        this.bot = bot; this.sessionService = sessionService;
    }

    public void sendMainMenu(String chatId) {
        // Сброс состояния пользователя при входе в главное меню
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
        rowInline1.add(InlineKeyboardButton.builder()
                .text("Просмотренные фильмы")
                .callbackData("view_watched_movies")
                .build());
        rowsInline.add(rowInline1);

        List<InlineKeyboardButton> rowInline2 = new ArrayList<>();
        rowInline2.add(InlineKeyboardButton.builder()
                .text("Друзья")
                .callbackData("friends_menu")
                .build());
        rowInline2.add(InlineKeyboardButton.builder()
                .text("Запросы в друзья")
                .callbackData("friend_requests_menu")
                .build());
        rowsInline.add(rowInline2);

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

    public void sendPlannedMoviesMenu(String chatId, Set<Movie> plannedMovies) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        for (Movie movie : plannedMovies) {
            List<InlineKeyboardButton> rowInline = new ArrayList<>();
            InlineKeyboardButton removeButton = new InlineKeyboardButton("Удалить: " + movie.getTitle());
            removeButton.setCallbackData("remove_planned:" + movie.getImdbId());
            rowInline.add(removeButton);
            rowsInline.add(rowInline);
        }
        markupInline.setKeyboard(rowsInline);
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Ваши запланированные фильмы:");
        message.setReplyMarkup(markupInline);
        try {
            bot.execute(message);
            logger.info("Planned movies menu sent to chatId: {}", chatId);
        } catch (TelegramApiException e) {
            logger.error("Error sending planned movies menu to chatId: {}", chatId, e);
        }
    }


    public void sendMovieSelectionMessage(String chatId, List<Movie> movies) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        for (Movie movie : movies) {
            List<InlineKeyboardButton> rowInline = new ArrayList<>();
            InlineKeyboardButton button = new InlineKeyboardButton(movie.getTitle() + " (" + movie.getYear() + ")");
            button.setCallbackData("add_movie:" + movie.getImdbId());
            rowInline.add(button);
            rowsInline.add(rowInline);
        }
        markupInline.setKeyboard(rowsInline);
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Выберите фильм для добавления в список просмотренных:");
        message.setReplyMarkup(markupInline);
        try {
            bot.execute(message);
            logger.info("Movie selection message sent to chatId: {}", chatId);
        } catch (TelegramApiException e) {
            logger.error("Error sending movie selection message to chatId: {}", chatId, e);
        }
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

    public void showWatchedMoviesList(String chatId, List<Movie> watchedMovies) {
        StringBuilder response = new StringBuilder("Ваши просмотренные фильмы:\n");
        for (int i = 0; i < watchedMovies.size(); i++) {
            response.append(i + 1).append(". ").append(watchedMovies.get(i).getTitle()).append(" (").append(watchedMovies.get(i).getYear()).append(")\n");
        }
        sendMessage(chatId, response.toString());
        sessionService.setUserState(chatId, UserStateEnum.AWAITING_MOVIE_SELECTION);
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
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        for (AppUser friend : friends) {
            List<InlineKeyboardButton> rowInline = new ArrayList<>();
            InlineKeyboardButton viewFriendButton = new InlineKeyboardButton();
            viewFriendButton.setText("Удалить друга: " + friend.getUsername());
            viewFriendButton.setCallbackData("remove_friend:" + friend.getUsername());
            rowInline.add(viewFriendButton);
            rowsInline.add(rowInline);
        }
        markupInline.setKeyboard(rowsInline);
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Ваши друзья:");
        message.setReplyMarkup(markupInline);
        try {
            bot.execute(message);
            logger.info("Friends menu sent to chatId: {}", chatId);
        } catch (TelegramApiException e) {
            logger.error("Error sending friends menu to chatId: {}", chatId, e);
        }
    }

   /* public void sendPlannedMoviesMenu(String chatId, Set<Movie> plannedMovies) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        for (Movie movie : plannedMovies) {
            List<InlineKeyboardButton> rowInline = new ArrayList<>();
            InlineKeyboardButton removeButton = new InlineKeyboardButton();
            removeButton.setText("Удалить: " + movie.getTitle());
            removeButton.setCallbackData("remove_planned:" + movie.getImdbId());
            rowInline.add(removeButton);
            rowsInline.add(rowInline);
        }
        markupInline.setKeyboard(rowsInline);
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Ваши запланированные фильмы:");
        message.setReplyMarkup(markupInline);
        try {
            bot.execute(message);
            logger.info("Planned movies menu sent to chatId: {}", chatId);
        } catch (TelegramApiException e) {
            logger.error("Error sending planned movies menu to chatId: {}", chatId, e);
        }
    }

    */
}
