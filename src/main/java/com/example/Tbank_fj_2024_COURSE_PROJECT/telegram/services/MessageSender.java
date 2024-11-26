package com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services;

import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.MovieBot;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.movie.Movie;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.movie.MovieStatus;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.movie.UserMovie;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.user.AppUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;

@Component
public class MessageSender  {

    private static final Logger logger = LoggerFactory.getLogger(MessageSender.class);

    private final MovieBot movieBot;
    private final SessionService sessionService;
    private final ObjectMapper objectMapper;

    @Autowired
    public MessageSender(@Lazy MovieBot bot, SessionService sessionService, ObjectMapper objectMapper) {
        this.movieBot = bot;
        this.sessionService = sessionService;
        this.objectMapper = objectMapper;
    }

    // Основное меню
    public void sendMainMenu(String chatId) {
        sessionService.clearUserState(chatId);
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("📋 Выберите действие из меню ниже:");

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        // Кнопки основного меню
        rowsInline.add(createButtonRow("➕ Добавить фильм", "add_movie"));
        rowsInline.add(createButtonRow("✔️ Просмотренные фильмы", "view_watched_movies"));
        rowsInline.add(createButtonRow("📋 Запланированные фильмы", "view_planned_movies"));
        rowsInline.add(createButtonRow("👥 Друзья", "friends_menu"));

        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);

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
        sb.append("\nЧтобы выбрать фильм, введите его номер.");

        message.setText(sb.toString());

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        buttons.add(
                List.of(
                        InlineKeyboardButton.builder()
                                .text("🏠 Главное меню")
                                .callbackData("main_menu")
                                .build()
                )
        );
        inlineKeyboardMarkup.setKeyboard(buttons);
        message.setReplyMarkup(inlineKeyboardMarkup);

        try {
            movieBot.execute(message);
            logger.info("Watched movies message sent to chatId: {}", chatId);
        } catch (TelegramApiException e) {
            logger.error("Error sending watched movies to chatId: {}", chatId, e);
        }
    }

    public void sendPlannedMovies(String chatId, List<UserMovie> combinedPlannedMovies, AppUser currentUser) {
        if (combinedPlannedMovies.isEmpty()) {
            sendMessage(chatId, "У вас нет запланированных фильмов.");
            sendMainMenu(chatId);
            return;
        }

        Set<String> addedMovieIds = new HashSet<>();
        StringBuilder response = new StringBuilder("Запланированные фильмы (ваши и предложенные друзьями):\n");

        int index = 1;
        for (UserMovie userMovie : combinedPlannedMovies) {
            Movie movie = userMovie.getMovie();
            String suggestedBy = userMovie.getSuggestedBy();

            if (addedMovieIds.add(movie.getImdbId())) {
                response.append(index++).append(". ").append(movie.getTitle())
                        .append(" (").append(movie.getYear()).append(")");

                if (userMovie.getStatus() == MovieStatus.WANT_TO_WATCH) {
                    response.append(" — запланировано вами\n");
                } else if (userMovie.getStatus() == MovieStatus.WANT_TO_WATCH_BY_FRIEND) {
                    response.append(" — предложено другом ")
                            .append(suggestedBy != null ? suggestedBy : "неизвестным пользователем").append("\n");
                }
            }
        }

        response.append("\nЧтобы выбрать фильм, введите его номер.");

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        // Добавляем кнопку "Главное меню"
        rowsInline.add(
                List.of(
                        InlineKeyboardButton.builder()
                                .text("🏠 Главное меню")
                                .callbackData("main_menu")
                                .build()
                )
        );
        markupInline.setKeyboard(rowsInline);

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(response.toString());
        message.setReplyMarkup(markupInline);

        try {
            movieBot.execute(message);
            logger.info("Planned movies message sent to chatId: {}", chatId);
        } catch (TelegramApiException e) {
            logger.error("Error sending planned movies to chatId: {}", chatId, e);
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

    // Детали о просмотренном фильме с оценками и кнопками действий
    public void sendMovieDetails(String chatId, Movie movie, Double userRating, double averageFriendRating) {

        // Формируем текст сообщения
        String messageText = String.format(
                "Название: %s\nГод: %s\nМоя оценка: %s\nСредняя оценка среди друзей: %.2f\nОценка IMDb: %s",
                movie.getTitle(),
                movie.getYear(),
                (userRating != null ? userRating.toString() : "Не оценено"),
                averageFriendRating,
                movie.getImdbRating()
        );

        // Формируем кнопки
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();

        // Первый ряд кнопок
        buttons.add(
                List.of(
                        InlineKeyboardButton.builder()
                                .text("❌ Удалить")
                                .callbackData("delete_movie")
                                .build(),
                        InlineKeyboardButton.builder()
                                .text("⭐ Оценка")
                                .callbackData("rate_movie")
                                .build()
                )
        );

        // Второй ряд кнопок
        buttons.add(
                List.of(
                        InlineKeyboardButton.builder()
                                .text("🏠 Главное меню")
                                .callbackData("main_menu")
                                .build()
                )
        );

        // Если есть постер, отправляем его вместе с текстом и кнопками
        if (movie.getPoster() != null && !movie.getPoster().isEmpty() && !movie.getPoster().equals("N/A")) {
            movieBot.handlePhotoMessage(chatId, movie.getPoster(), messageText, buttons);
        } else {
            // Если постера нет, просто отправляем текст с кнопками
            sendMessageWithInlineKeyboard(chatId, messageText, buttons);
        }

        // Сохраняем выбранный фильм в сессии
        sessionService.setSelectedMovie(chatId, movie);
    }


    // Детали о запланированном фильме с оценками и кнопками действий
    public void sendPlannedMovieDetailsWithOptions(String chatId, AppUser user, Movie movie, int userHype, double averageFriendHype, boolean isOwnMovie) {
        // Формируем текст сообщения
        String message = String.format(
                "Название: %s\nГод: %s\nМой ажиотаж: %d\nАжиотаж среди друзей: %.2f\nОценка IMDb: %s",
                movie.getTitle(),
                movie.getYear(),
                userHype,
                averageFriendHype,
                movie.getImdbRating()
        );

        // Формируем кнопки
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();

        // Первый ряд кнопок
        if (isOwnMovie) {
            buttons.add(
                    List.of(
                            InlineKeyboardButton.builder()
                                    .text("❌ Удалить")
                                    .callbackData("delete_planned")
                                    .build(),
                            InlineKeyboardButton.builder()
                                    .text("🔥 Ажиотаж")
                                    .callbackData("add_hype")
                                    .build()
                    )
            );
        } else {
            buttons.add(
                    List.of(
                            InlineKeyboardButton.builder()
                                    .text("🔥 Ажиотаж")
                                    .callbackData("add_hype")
                                    .build()
                    )
            );
        }

        // Второй ряд кнопок
        buttons.add(
                List.of(
                        InlineKeyboardButton.builder()
                                .text("🏠 Главное меню")
                                .callbackData("main_menu")
                                .build()
                )
        );

        // Если есть постер, отправляем его вместе с текстом и кнопками
        if (movie.getPoster() != null && !movie.getPoster().isEmpty() && !movie.getPoster().equals("N/A")) {
            movieBot.handlePhotoMessage(chatId, movie.getPoster(), message, buttons);
        } else {
            // Если постера нет, просто отправляем текст с кнопками
            sendMessageWithInlineKeyboard(chatId, message, buttons);
        }

        // Сохраняем выбранный фильм в сессии
        sessionService.setSelectedMovie(chatId, movie);
    }

    // Отправка inline-клавиатуры с выбором статуса фильма
    public void processAddMovieStatusSelection(String chatId) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        // Первый ряд кнопок: Запланировать и Просмотрен
        rowsInline.add(
                List.of(
                        InlineKeyboardButton.builder()
                                .text("📋 Запланировать")
                                .callbackData("selected_planned")
                                .build(),
                        InlineKeyboardButton.builder()
                                .text("✔️ Просмотрен")
                                .callbackData("selected_watched")
                                .build()
                )
        );

        // Второй ряд кнопок: Главное меню
        rowsInline.add(
                List.of(
                        InlineKeyboardButton.builder()
                                .text("🏠 Главное меню")
                                .callbackData("main_menu")
                                .build()
                )
        );

        sessionService.setUserState(chatId, UserStateEnum.WAITING_FOR_MOVIE_TITLE);
        markupInline.setKeyboard(rowsInline);

        SendMessage message = new SendMessage(chatId, "🎬 Выберите статус для фильма:");
        message.setReplyMarkup(markupInline);

        sendMessage(message);
    }

    // Меню для входящих и исходящих запросов в друзья
    public void sendFriendRequestsMenu(String chatId, List<AppUser> friendRequests, boolean isIncoming) {
        String header = isIncoming ? "📥 Ваши входящие запросы:" : "📤 Ваши исходящие запросы:";
        SendMessage message = new SendMessage(chatId, header);

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        for (AppUser request : friendRequests) {
            List<InlineKeyboardButton> rowInline = new ArrayList<>();
            if (isIncoming) {
                rowInline.add(createButton("✔️ Принять: " + request.getUsername(), "accept_request:" + request.getUsername()));
                rowInline.add(createButton("❌ Отклонить: " + request.getUsername(), "reject_request:" + request.getUsername()));
            } else {
                rowInline.add(createButton("❌ Отменить: " + request.getUsername(), "cancel_request:" + request.getUsername()));
            }
            rowsInline.add(rowInline);
        }

        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);

        sendMessage(message);
    }

    // Меню друзей
    public void sendFriendsMenu(String chatId, List<AppUser> friends) {
        StringBuilder response = new StringBuilder("👥 Ваши друзья:\n");
        for (int i = 0; i < friends.size(); i++) {
            response.append(i + 1).append(". ").append(friends.get(i).getUsername()).append("\n");
        }

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        if (!friends.isEmpty()) {
            rowsInline.add(createButtonRow("❌ Удалить друга", "delete_friend"));
        }
        rowsInline.add(createButtonRow("➕ Отправить запрос", "send_friend_request"));
        rowsInline.add(Arrays.asList(
                createButton("📥 Входящие заявки", "incoming_requests"),
                createButton("📤 Исходящие заявки", "outgoing_requests")
        ));
        rowsInline.add(createButtonRow("🏠 Главное меню", "main_menu"));

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

    // Отправка сообщения, упрощения для этого класса
    private void sendMessage(SendMessage message) {
        try {
            movieBot.execute(message);
            logger.info("Message sent to chatId: {}", message.getChatId());
        } catch (TelegramApiException e) {
            logger.error("Error sending message to chatId: {}", message.getChatId(), e);
        }
    }

    // Отправка сообщения
    public void sendMessage(String chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        sendMessage(message);
    }

    private void sendMessageWithInlineKeyboard(String chatId, String text, List<List<InlineKeyboardButton>> buttons) {
        SendMessage message = new SendMessage(chatId, text);
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(buttons);
        message.setReplyMarkup(inlineKeyboardMarkup);
        sendMessage(message);
    }


}
