package com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.handlers;

import com.example.Tbank_fj_2024_COURSE_PROJECT.models.movie.MovieStatus;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.movie.UserMovie;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.user.AppUser;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.movie.Movie;
import com.example.Tbank_fj_2024_COURSE_PROJECT.repositories.MovieRepository;
import com.example.Tbank_fj_2024_COURSE_PROJECT.services.*;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.MessageSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class    CommandHandler {

    private static final Logger logger = LoggerFactory.getLogger(CommandHandler.class);

    private final TelegramAuthService telegramAuthService;
    private final AppUserService appUserService;
    private final SessionService sessionService;
    private final OmdbService omdbService;
    private final MessageSender messageSender;
    private final FriendshipService friendshipService;
    private final UserMovieService userMovieService;
    private final MovieService movieService;



    @Autowired
    public CommandHandler(
            @Lazy TelegramAuthService telegramAuthService,
            @Lazy AppUserService appUserService,
            @Lazy SessionService sessionService,
            @Lazy OmdbService omdbService,
            @Lazy MessageSender messageSender,
            @Lazy FriendshipService friendshipService,
            @Lazy UserMovieService userMovieService,
            @Lazy MovieRepository movieRepository,
            @Lazy MovieService movieService)
    {
        this.telegramAuthService = telegramAuthService;
        this.appUserService = appUserService;
        this.sessionService = sessionService;
        this.omdbService = omdbService;
        this.messageSender = messageSender;
        this.friendshipService = friendshipService;
        this.userMovieService = userMovieService;
        this.movieService = movieService;
    }

    public void handleUserCommand(String chatId, String command, List<String> args) {
        AppUser currentUser = sessionService.getCurrentUser(chatId);
        if (currentUser == null) {
            messageSender.sendMessage(chatId, "Вы не авторизованы. Используйте /login для входа или /register для регистрации.");
            return;
        }

        switch (command) {

            case "add_friend":
                sessionService.setUserState(chatId, UserStateEnum.WAITING_FOR_FRIEND_USERNAME);
                messageSender.sendMessage(chatId, "Введите имя друга для добавления:");
                break;
            case "main_menu":
                sessionService.clearUserState(chatId);
                messageSender.sendMainMenu(chatId);
                break;
            default:
                messageSender.sendMessage(chatId, "Неизвестная команда.");
                break;
        }
    }

    public void handleStateBasedCommand(String chatId, String messageText, UserStateEnum state) {
        logger.info("Текущее состояние для chatId {}: {}", chatId, state);

        if (state == UserStateEnum.DEFAULT_UNLOGGED) {
            handleUnloggedState(chatId, messageText);
            return;
        }

        switch (state) {
            case WAITING_FOR_MOVIE_TITLE_PLANNED:
                processAddMovie(chatId, messageText, true);  // true для запланированных
                break;

            case WAITING_FOR_MOVIE_TITLE_WATCHED:
                processAddMovie(chatId, messageText, false); // false для просмотренных
                break;
            case WAITING_FOR_FRIEND_USERNAME:
                processAddFriend(chatId, messageText);
                sessionService.clearUserState(chatId);  // Сбрасываем состояние
                break;
            case AWAITING_FRIEND_DELETION:
                processDeleteFriend(chatId, messageText); // обработка удаления друга
                sessionService.clearUserState(chatId);
                break;
            case AWAITING_PLANNED_MOVIE_SELECTION:
                try {
                    int movieIndex = Integer.parseInt(messageText);
                    handlePlannedMovieSelection(chatId, movieIndex); // Передаем индекс фильма
                } catch (NumberFormatException e) {
                    messageSender.sendMessage(chatId, "Введите корректный номер фильма.");
                }
                break;
            case AWAITING_MOVIE_SELECTION_USER:  // Обрабатываем фильм из списка просмотренных
                try {
                    int movieIndex = Integer.parseInt(messageText);
                    handleMovieSelection(chatId, movieIndex); // Передаем индекс фильма
                } catch (NumberFormatException e) {
                    messageSender.sendMessage(chatId, "Введите корректный номер фильма.");
                }
                break;
            case AWAITING_MOVIE_HYPE:
                processMovieHype(chatId,messageText);
                break;
            case AWAITING_MOVIE_RATING:
                processMovieRating(chatId, messageText);
                break;
            case DEFAULT_LOGGED:
                messageSender.sendMessage(chatId, "Вы авторизованы. Пожалуйста, выберите действие.");
                messageSender.sendMainMenu(chatId);
                break;
            default:
                messageSender.sendMessage(chatId, "Неизвестное состояние.");
                break;
        }
    }




    public void processAddMovie(String chatId, String title, boolean isPlanned) {
        AppUser currentUser = sessionService.getCurrentUser(chatId);
        if (currentUser != null) {
            List<Movie> movies = omdbService.searchMoviesByTitle(title);
            if (!movies.isEmpty()) {
                // Отправляем простой список фильмов
                messageSender.sendSimpleMovieList(chatId, movies);
                // Сохраняем информацию о статусе в sessionService для использования при выборе фильма
                sessionService.setUserState(chatId, isPlanned ? UserStateEnum.WAITING_FOR_MOVIE_TITLE_PLANNED : UserStateEnum.WAITING_FOR_MOVIE_TITLE_WATCHED);
            } else {
                messageSender.sendMessage(chatId, "Фильмы по запросу \"" + title + "\" не найдены.");
                messageSender.sendMainMenu(chatId);
            }
        } else {
            messageSender.sendMessage(chatId, "Вы не авторизованы. Используйте /login для входа.");
        }
    }

    public void processMovieSelection(String chatId, String imdbId, boolean isPlanned) {
        AppUser currentUser = sessionService.getCurrentUser(chatId);
        logger.info(sessionService.getUserState(chatId) + " Мы заходим в processMovieSelection");

        if (currentUser != null) {
            Movie movie = movieService.findMovieByImdbId(imdbId)
                    .orElseGet(() -> {
                        Movie newMovie = omdbService.getMovieByImdbId(imdbId);
                        if (newMovie != null) {
                            movieService.saveMovie(newMovie);
                        }
                        return newMovie;
                    });

            if (movie != null) {
                try {
                    if (isPlanned) {
                        // Добавляем фильм пользователю в запланированные
                        userMovieService.addPlannedMovie(currentUser, movie);
                        messageSender.sendMessage(chatId, "Фильм успешно добавлен в список запланированных.");

                        // Получаем список друзей пользователя
                        List<AppUser> friends = friendshipService.getFriends(currentUser.getUsername());

                        // Добавляем фильм каждому другу с статусом WANT_TO_WATCH_BY_FRIEND
                        for (AppUser friend : friends) {
                            userMovieService.addSuggestedMovie(friend, movie, currentUser.getUsername());
                        }

                        messageSender.sendMessage(chatId, "Фильм добавлен в списки друзей как предложенный вами.");
                    } else {
                        // Если фильм добавляется как просмотренный, добавляем только текущему пользователю
                        userMovieService.addWatchedMovie(currentUser, movie, chatId);
                        messageSender.sendMessage(chatId, "Фильм успешно добавлен в список просмотренных.");
                    }

                    messageSender.sendMainMenu(chatId);
                } catch (IllegalArgumentException e) {
                    messageSender.sendMessage(chatId, e.getMessage());
                    sessionService.clearUserState(chatId);
                }
            } else {
                messageSender.sendMessage(chatId, "Ошибка: фильм с таким IMDb ID не найден.");
                sessionService.clearUserState(chatId);
            }
        } else {
            messageSender.sendMessage(chatId, "Вы не авторизованы. Используйте /login для входа.");
        }
    }


    @Transactional
    public void handleMovieSelection(String chatId, int movieIndex) {
        AppUser currentUser = sessionService.getCurrentUser(chatId);
        if (currentUser != null) {
            List<UserMovie> watchedMovies = userMovieService.getWatchedMovies(currentUser);
            if (movieIndex < 1 || movieIndex > watchedMovies.size()) {
                messageSender.sendMessage(chatId, "Некорректный номер. Попробуйте снова.");
            } else {
                UserMovie selectedMovie = watchedMovies.get(movieIndex - 1);
                double averageFriendRating = userMovieService.getAverageFriendRating(currentUser, selectedMovie.getMovie());
                messageSender.sendMovieDetails(chatId, selectedMovie.getMovie(), selectedMovie.getRating(), averageFriendRating);

                // Сохраняем выбранный фильм и устанавливаем состояние
                sessionService.setSelectedMovie(chatId, selectedMovie.getMovie());
                sessionService.setUserState(chatId, UserStateEnum.AWAITING_MOVIE_SELECTION_USER);
            }
        } else {
            messageSender.sendMessage(chatId, "Вы не авторизованы. Используйте /login для входа.");
        }
    }

    @Transactional
    public void handlePlannedMovieSelection(String chatId, int movieIndex) {
        AppUser currentUser = sessionService.getCurrentUser(chatId);
        if (currentUser != null) {
            List<UserMovie> combinedPlannedMovies = userMovieService.getCombinedPlannedMovies(currentUser);
            if (movieIndex < 1 || movieIndex > combinedPlannedMovies.size()) {
                messageSender.sendMessage(chatId, "Некорректный номер. Попробуйте снова.");
            } else {
                UserMovie selectedUserMovie = combinedPlannedMovies.get(movieIndex - 1);
                Movie selectedMovie = selectedUserMovie.getMovie();
                AppUser movieOwner = selectedUserMovie.getUser();

                // Проверка на владельца
                boolean isOwnMovie = userMovieService.isMovieOwner(currentUser, selectedMovie);

                logger.info("Selected movie: {}, Owner: {}, Current user: {}", selectedMovie.getTitle(), movieOwner.getUsername(), currentUser.getUsername());

                int userHype = (selectedUserMovie.getHype() != null) ? selectedUserMovie.getHype() : 0;
                double averageFriendHype = userMovieService.getAverageFriendHype(currentUser, selectedMovie);

                // Передаем параметр isOwnMovie, чтобы кнопка удаления отображалась только для собственных фильмов
                messageSender.sendPlannedMovieDetailsWithOptions(chatId, currentUser, selectedMovie, userHype, averageFriendHype, isOwnMovie);
                sessionService.setSelectedMovie(chatId, selectedMovie);
                sessionService.setUserState(chatId, UserStateEnum.AWAITING_MOVIE_SELECTION_USER);
            }
        } else {
            messageSender.sendMessage(chatId, "Вы не авторизованы. Используйте /login для входа.");
        }
    }




    @Transactional
    public void processAddFriend(String chatId, String friendUsername) {
        try {
            AppUser currentUser = sessionService.getCurrentUser(chatId);
            friendshipService.addFriendRequest(currentUser.getUsername(), friendUsername);
            messageSender.sendMessage(chatId, "Запрос на добавление в друзья отправлен!");
        } catch (IllegalArgumentException e) {
            messageSender.sendMessage(chatId, "Ошибка: " + e.getMessage());
        }
    }
    @Transactional
    public void showWatchedMoviesList(String chatId, List<Movie> watchedMovies) {
        StringBuilder response = new StringBuilder("Ваши просмотренные фильмы:\n");
        for (int i = 0; i < watchedMovies.size(); i++) {
            response.append(i + 1).append(". ").append(watchedMovies.get(i).getTitle()).append(" (").append(watchedMovies.get(i).getYear()).append(")\n");
        }
        messageSender.sendMessage(chatId, response.toString());
        messageSender.sendMessage(chatId, "Введите номер фильма, чтобы просмотреть его детали:");
        sessionService.setUserState(chatId, UserStateEnum.AWAITING_MOVIE_SELECTION_USER);
    }

    @Transactional
    public void handleShowCombinedPlannedMovies(String chatId) {
        AppUser currentUser = sessionService.getCurrentUser(chatId);
        if (currentUser != null) {
            List<UserMovie> combinedPlannedMovies = userMovieService.getCombinedPlannedMovies(currentUser);

            Set<String> addedMovieIds = new HashSet<>();
            StringBuilder response = new StringBuilder("Запланированные фильмы (ваши и предложенные друзьями):\n");

            int index = 1;

            for (UserMovie userMovie : combinedPlannedMovies) {
                Movie movie = userMovie.getMovie();
                MovieStatus status = userMovie.getStatus();
                String suggestedBy = userMovie.getSuggestedBy();

                if (addedMovieIds.add(movie.getImdbId())) {
                    response.append(index++).append(". ").append(movie.getTitle())
                            .append(" (").append(movie.getYear()).append(")");

                    if (status == MovieStatus.WANT_TO_WATCH) {
                        response.append(" — запланировано вами\n");
                    } else if (status == MovieStatus.WANT_TO_WATCH_BY_FRIEND) {
                        response.append(" — предложено другом ")
                                .append(suggestedBy != null ? suggestedBy : "неизвестным пользователем")
                                .append("\n");
                    }
                }
            }

            messageSender.sendMessage(chatId, response.toString());
            sessionService.setUserState(chatId, UserStateEnum.AWAITING_PLANNED_MOVIE_SELECTION);
        } else {
            messageSender.sendMessage(chatId, "Вы не авторизованы. Используйте /login для входа.");
        }
    }




    @Transactional
    public void handleDeleteMovie(String chatId) {
        AppUser currentUser = sessionService.getCurrentUser(chatId);
        Movie selectedMovie = sessionService.getSelectedMovie(chatId);
        if (currentUser != null && selectedMovie != null) {
            userMovieService.setMovieStatusForUser(currentUser, selectedMovie, MovieStatus.UNWATCHED);
            messageSender.sendMessage(chatId, "Фильм успешно удален из просмотренных.");
            sessionService.setSelectedMovie(chatId, null);
            messageSender.sendMainMenu(chatId);
        } else {
            messageSender.sendMessage(chatId, "Ошибка изменения статуса. Попробуйте снова.");
        }
    }

    @Transactional
    public void handleDeletePlannedMovie(String chatId) {
        AppUser currentUser = sessionService.getCurrentUser(chatId);
        Movie selectedMovie = sessionService.getSelectedMovie(chatId);
        if (currentUser != null && selectedMovie != null) {
            userMovieService.removePlannedMovieAndUpdateFriends(currentUser, selectedMovie);
            messageSender.sendMessage(chatId, "Фильм успешно удален из запланированных и обновлен статус у друзей.");
            sessionService.setSelectedMovie(chatId, null);
            messageSender.sendMainMenu(chatId);
        } else {
            messageSender.sendMessage(chatId, "Ошибка изменения статуса. Попробуйте снова.");
        }
    }

    public void handleRateMovie(String chatId) {
        messageSender.sendMessage(chatId, "Введите вашу оценку от 1.0 до 10.0 для выбранного фильма:");
        sessionService.setUserState(chatId, UserStateEnum.AWAITING_MOVIE_RATING);
    }

    public void processMovieRating(String chatId, String ratingText) {
        // Проверяем, что у пользователя действительно выбран фильм
        Movie movie = sessionService.getSelectedMovie(chatId);
        if (movie == null) {
            messageSender.sendMessage(chatId, "Ошибка: не выбран фильм для оценки.");
            sessionService.clearUserState(chatId);
            return;
        }

        try {
            double rating = Double.parseDouble(ratingText);
            if (rating < 1.0 || rating > 10.0) {
                messageSender.sendMessage(chatId, "Некорректное значение. Введите оценку от 1.0 до 10.0.");
                return;
            }

            AppUser user = sessionService.getCurrentUser(chatId);
            if (user != null) {
                userMovieService.addRating(user.getUsername(), movie.getImdbId(), rating);
                messageSender.sendMessage(chatId, "Оценка успешно добавлена.");
            }
        } catch (NumberFormatException e) {
            messageSender.sendMessage(chatId, "Некорректный формат числа. Введите оценку от 1.0 до 10.0.");
        }

        messageSender.sendMainMenu(chatId);
    }



    public void handleRegisterCommand(String chatId, String username, String password) {
        try {
            AppUser newUser = new AppUser();
            newUser.setUsername(username);
            newUser.setPassword(password);
            appUserService.registerUser(newUser, chatId);
            messageSender.sendMessage(chatId, "Пользователь успешно зарегистрирован. Используйте /login для входа.");
        } catch (IllegalArgumentException e) {
            messageSender.sendMessage(chatId, "Ошибка при регистрации: " + e.getMessage());
        }
    }

    public void handleLoginCommand(String chatId, String username, String password) {
        try {
            AppUser user = appUserService.findByUsername(username);
            if (appUserService.checkPassword(user, password)) {
                sessionService.createSession(chatId, user);
                sessionService.setUserState(chatId, UserStateEnum.DEFAULT_LOGGED); // Меняем состояние на LOGGED
                messageSender.sendMessage(chatId, "Вы успешно вошли как " + username + ".");
                messageSender.sendMainMenu(chatId);
            } else {
                messageSender.sendMessage(chatId, "Неверный пароль. Попробуйте снова.");
            }
        } catch (IllegalArgumentException e) {
            messageSender.sendMessage(chatId, "Ошибка входа: " + e.getMessage());
        }
    }

    public void handleUnloggedState(String chatId,  String messageText) {
            ParsedCommand parsedCommand = CommandParser.parse(messageText);
            String command = parsedCommand.getCommandName();
            List<String> args = parsedCommand.getArgs();

            if ("/login".equals(command)) {
                if (args.size() == 2) {
                    String username = args.get(0);
                    String password = args.get(1);
                    handleLoginCommand(chatId, username, password);
                } else {
                    messageSender.sendMessage(chatId, "Используйте формат: /login [username] [password]");
                }
            } else if ("/register".equals(command)) {
                if (args.size() == 2) {
                    String username = args.get(0);
                    String password = args.get(1);
                    handleRegisterCommand(chatId, username, password);
                } else {
                    messageSender.sendMessage(chatId, "Используйте формат: /register [username] [password]");
                }
            } else {
                messageSender.sendMessage(chatId, "Вы не авторизованы. Пожалуйста, используйте /login для входа или /register для регистрации.");
            }
            return;
        }



    public void handleIncomingRequestsCommand(String chatId) {
        AppUser currentUser = sessionService.getCurrentUser(chatId);
        if (currentUser != null) {
            messageSender.sendFriendRequestsMenu(chatId, friendshipService.getIncomingRequests(currentUser.getUsername()), true);
        } else {
            messageSender.sendMessage(chatId, "Вы не авторизованы. Используйте /login для входа.");
        }
    }

    public void handleOutgoingRequestsCommand(String chatId) {
        AppUser currentUser = sessionService.getCurrentUser(chatId);
        if (currentUser != null) {
            messageSender.sendFriendRequestsMenu(chatId, friendshipService.getOutgoingRequests(currentUser.getUsername()), false);
        } else {
            messageSender.sendMessage(chatId, "Вы не авторизованы. Используйте /login для входа.");
        }
    }


    @Transactional
    public void handleWatchedMoviesCommand(String chatId) {
        AppUser currentUser = sessionService.getCurrentUser(chatId);
        if (currentUser != null) {
            // Явно получаем обновленный список из базы данных
            List<Movie> watchedMovies = userMovieService.getWatchedMoviesByUser(currentUser.getId());
            if (watchedMovies.isEmpty()) {
                messageSender.sendMessage(chatId, "У вас нет просмотренных фильмов.");
                messageSender.sendMainMenu(chatId);
            } else {
                // Отправляем актуальный список
                showWatchedMoviesList(chatId, watchedMovies);
                sessionService.setUserState(chatId, UserStateEnum.AWAITING_MOVIE_SELECTION_USER);
            }
        } else {
            messageSender.sendMessage(chatId, "Вы не авторизованы. Используйте /login для входа.");
        }

    }



    public void handleFriendsMenu(String chatId) {
        AppUser currentUser = sessionService.getCurrentUser(chatId);
        if (currentUser != null) {
            messageSender.sendFriendsMenu(chatId, friendshipService.getFriends(currentUser.getUsername()));
        } else {
            messageSender.sendMessage(chatId, "Вы не авторизованы. Используйте /login для входа.");
        }
    }



    public void handleAcceptRequest(String chatId, String friendUsername) {
        AppUser currentUser = sessionService.getCurrentUser(chatId);
        if (currentUser != null) {
            friendshipService.acceptFriendRequest(currentUser.getUsername(), friendUsername);
            messageSender.sendMessage(chatId, "Запрос от " + friendUsername + " принят.");
        } else {
            messageSender.sendMessage(chatId, "Вы не авторизованы. Используйте /login для входа.");
        }
    }

    public void handleRejectRequest(String chatId, String friendUsername) {
        AppUser currentUser = sessionService.getCurrentUser(chatId);
        if (currentUser != null) {
            friendshipService.rejectFriendRequest(currentUser.getUsername(), friendUsername);
            messageSender.sendMessage(chatId, "Запрос от " + friendUsername + " отклонен.");
            messageSender.sendMainMenu(chatId);
        } else {
            messageSender.sendMessage(chatId, "Вы не авторизованы. Используйте /login для входа.");
        }
    }
    public void handleDeleteFriend(String chatId) {
        AppUser currentUser = sessionService.getCurrentUser(chatId);
        if (currentUser != null) {
            sessionService.setUserState(chatId, UserStateEnum.AWAITING_FRIEND_DELETION);
            messageSender.sendMessage(chatId, "Введите имя друга для удаления:");
        } else {
            messageSender.sendMessage(chatId, "Вы не авторизованы. Используйте /login для входа.");
        }
    }

    public void processDeleteFriend(String chatId, String friendUsername) {
        AppUser currentUser = sessionService.getCurrentUser(chatId);
        if (currentUser != null) {
            try {
                friendshipService.removeFriendship(currentUser.getUsername(), friendUsername);
                messageSender.sendMessage(chatId, "Дружба с " + friendUsername + " была успешно удалена. Статус предложенных фильмов обновлен.");
                messageSender.sendMainMenu(chatId);
            } catch (IllegalArgumentException e) {
                messageSender.sendMessage(chatId, "Ошибка: " + e.getMessage());
            }
        } else {
            messageSender.sendMessage(chatId, "Вы не авторизованы. Используйте /login для входа.");
        }
    }

    public void handleCancelRequest(String chatId, String friendUsername) {
        AppUser currentUser = sessionService.getCurrentUser(chatId);
        if (currentUser != null) {
            friendshipService.cancelFriendRequest(currentUser.getUsername(), friendUsername);
            messageSender.sendMessage(chatId, "Запрос в друзья для " + friendUsername + " отменен.");
            messageSender.sendMainMenu(chatId);
        } else {
            messageSender.sendMessage(chatId, "Вы не авторизованы. Используйте /login для входа.");
        }
    }

    public void handleSetHypeCommand(String chatId, String imdbId) {
        Movie selectedMovie = movieService.findMovieByImdbId(imdbId)
                .orElseThrow(() -> new IllegalArgumentException("Фильм не найден"));

        sessionService.setSelectedMovie(chatId, selectedMovie);
        messageSender.sendMessage(chatId, "Введите уровень ажиотажа от 0 до 100 для выбранного фильма:");
        sessionService.setUserState(chatId, UserStateEnum.AWAITING_MOVIE_HYPE);
    }

    public void processMovieHype(String chatId, String hypeText) {
        AppUser currentUser = sessionService.getCurrentUser(chatId);
        Movie selectedMovie = sessionService.getSelectedMovie(chatId);
        if (selectedMovie == null) {
            messageSender.sendMessage(chatId, "Ошибка: не выбран фильм для оценки ажиотажа.");
            sessionService.clearUserState(chatId);
            return;
        }

        try {
            int hype = Integer.parseInt(hypeText);
            if (hype < 0 || hype > 100) {
                messageSender.sendMessage(chatId, "Некорректное значение. Введите ажиотаж от 0 до 100.");
                return;
            }

            AppUser user = sessionService.getCurrentUser(chatId);
            if (user != null) {
                userMovieService.addHype(currentUser, selectedMovie, hype);
                messageSender.sendMessage(chatId, "Уровень ажиотажа успешно добавлен.");
            }
        } catch (NumberFormatException e) {
            messageSender.sendMessage(chatId, "Некорректный формат числа. Введите значение от 0 до 100.");
        }

        sessionService.clearUserState(chatId);
        messageSender.sendMainMenu(chatId);
    }

    @Transactional
    public void handleDeletePlannedMovie(String chatId, String imdbId) {
        AppUser currentUser = sessionService.getCurrentUser(chatId);
        Movie movie = movieService.findMovieByImdbId(imdbId)
                .orElseThrow(() -> new IllegalArgumentException("Фильм не найден"));

        userMovieService.removePlannedMovieAndUpdateFriends(currentUser, movie);
        messageSender.sendMessage(chatId, "Фильм успешно удален из запланированных и обновлен статус у друзей.");
        sessionService.clearUserState(chatId);
        messageSender.sendMainMenu(chatId);
    }




    @CacheEvict(value = "moviesCache", allEntries = true)
    public void clearAllCaches() {
        logger.info("Все кэши очищены!");
    }
}
