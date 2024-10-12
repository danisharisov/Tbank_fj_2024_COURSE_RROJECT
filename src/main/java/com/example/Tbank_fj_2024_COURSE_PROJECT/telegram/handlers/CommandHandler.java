package com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.handlers;

import com.example.Tbank_fj_2024_COURSE_PROJECT.models.movie.MovieStatus;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.movie.UserMovie;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.user.AppUser;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.movie.Movie;
import com.example.Tbank_fj_2024_COURSE_PROJECT.repositories.MovieRepository;
import com.example.Tbank_fj_2024_COURSE_PROJECT.services.*;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.MessageSender;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

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

    public void handleMessage(String chatId, String messageText) {
        SessionService.UserState userState = sessionService.getUserState(chatId);

        // Если состояние не найдено, создаём и сохраняем начальное состояние
        if (userState == null || UserStateEnum.DEFAULT.equals(userState.getState())) {
            logger.warn("Состояние пользователя для chatId {} отсутствует или сброшено. Устанавливаем начальное состояние.", chatId);
            userState = new SessionService.UserState(UserStateEnum.DEFAULT, "");
            sessionService.saveUserState(chatId, userState);
        }

        // Логируем текущее состояние
        logger.info("Текущее состояние пользователя chatId {}: {}", chatId, userState.getState());

        UserStateEnum state = userState.getState();

        switch (state) {
            case WAITING_FOR_MOVIE_TITLE:
                processAddMovie(chatId, messageText);
                sessionService.clearUserState(chatId);
                break;
            case  WAITING_FOR_FRIEND_USERNAME:
                processAddFriend(chatId, messageText);
                sessionService.clearUserState(chatId);
                break;
            case AWAITING_MOVIE_SELECTION:
                try {
                    int movieIndex = Integer.parseInt(messageText.trim());
                    handleMovieSelection(chatId, movieIndex);
                } catch (NumberFormatException e) {
                    messageSender.sendMessage(chatId, "Пожалуйста, введите корректный номер фильма.");
                }
                break;
            case AWAITING_MOVIE_RATING:
                processMovieRating(chatId, messageText);
                break;
            default:
                processCommand(chatId, messageText);
                break;
        }
    }

    private void processCommand(String chatId, String messageText) {
        // Получаем текущего пользователя из сессии
        AppUser currentUser = sessionService.getCurrentUser(chatId);

        // Разбираем команду и её аргументы
        ParsedCommand parsedCommand = CommandParser.parse(messageText);
        String command = parsedCommand.getCommandName();
        List<String> args = parsedCommand.getArgs();

        // Проверяем статус пользователя
        boolean isUserLoggedIn = currentUser != null;
        switch (command) {
            case "/login":
                if (args.size() == 2) {
                    String username = args.get(0);
                    String password = args.get(1);
                    handleLoginCommand(chatId, username, password);
                } else {
                    messageSender.sendMessage(chatId, "Используйте формат: /login [username] [password]");
                }
                break;

            case "/register":
                if (args.size() == 2) {
                    String username = args.get(0);
                    String password = args.get(1);
                    handleRegisterCommand(chatId, username, password);
                } else {
                    messageSender.sendMessage(chatId, "Используйте формат: /register [username] [password]");
                }
                break;

            case "add_movie":
                if (isUserLoggedIn) {
                    sessionService.setUserState(chatId, UserStateEnum.WAITING_FOR_MOVIE_TITLE);
                    messageSender.sendMessage(chatId, "Введите название фильма для добавления:");
                } else {
                    messageSender.sendMessage(chatId, "Вы не авторизованы. Используйте /login для входа или /register для регистрации.");
                }
                break;

            case "add_friend":
                if (isUserLoggedIn) {
                    sessionService.setUserState(chatId, UserStateEnum.WAITING_FOR_FRIEND_USERNAME);
                    messageSender.sendMessage(chatId, "Введите имя друга для добавления:");
                } else {
                    messageSender.sendMessage(chatId, "Вы не авторизованы. Используйте /login для входа или /register для регистрации.");
                }
                break;

            case "main_menu":
                if (isUserLoggedIn) {
                    sessionService.clearUserState(chatId);
                    messageSender.sendMainMenu(chatId);
                } else {
                    messageSender.sendMessage(chatId, "Вы не авторизованы. Используйте /login для входа или /register для регистрации.");
                }
                break;

            default:
                if (isUserLoggedIn) {
                    messageSender.sendMessage(chatId, "Неизвестная команда.");
                    sessionService.clearUserState(chatId);
                    messageSender.sendMainMenu(chatId);
                } else {
                    messageSender.sendMessage(chatId, "Используйте /register, чтобы зарегистрироваться, или /login, чтобы войти.");
                }
                break;
        }
    }


    public void processAddMovie(String chatId, String title) {
        AppUser currentUser = sessionService.getCurrentUser(chatId);
        if (currentUser != null) {
            // Выполняем поиск фильма по названию
            List<Movie> movies = omdbService.searchMoviesByTitle(title);
            if (!movies.isEmpty()) {
                // Отправляем список найденных фильмов для выбора
                messageSender.sendMovieSelectionMessage(chatId, movies);
                sessionService.setUserState(chatId, UserStateEnum.AWAITING_MOVIE_SELECTION);
            } else {
                messageSender.sendMessage(chatId, "Ошибка: фильмы по запросу \"" + title + "\" не найдены.");
            }
        } else {
            messageSender.sendMessage(chatId, "Вы не авторизованы. Используйте /login для входа.");
        }
    }

    public void processMovieSelection(String chatId, String imdbId) {
        AppUser currentUser = sessionService.getCurrentUser(chatId);
        if (currentUser != null) {
            // Поиск фильма в базе данных
            Movie movie = movieService.findMovieByImdbId(imdbId)
                    .orElseGet(() -> {
                        // Если фильма нет в базе, загружаем его через OMDb и сохраняем
                        Movie newMovie = omdbService.getMovieByImdbId(imdbId);
                        if (newMovie != null) {
                            movieService.saveMovie(newMovie);
                        }
                        return newMovie;
                    });

            if (movie != null) {
                try {
                    // Пытаемся добавить или обновить фильм в списке просмотренных
                    appUserService.addWatchedMovie(currentUser, movie, chatId);
                } catch (IllegalArgumentException e) {
                    // Обработка ошибок, если добавление не удалось
                    messageSender.sendMessage(chatId, e.getMessage());
                    sessionService.clearUserState(chatId);
                    messageSender.sendMainMenu(chatId);
                }
            } else {
                // Если фильм не найден, выводим сообщение
                messageSender.sendMessage(chatId, "Ошибка: фильм с таким IMDb ID не найден.");
                sessionService.clearUserState(chatId);
                messageSender.sendMainMenu(chatId);
            }
        } else {
            // Сообщение, если пользователь не авторизован
            messageSender.sendMessage(chatId, "Вы не авторизованы. Используйте /login для входа.");
            sessionService.clearUserState(chatId);
            messageSender.sendMainMenu(chatId);
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
                sessionService.setUserState(chatId, UserStateEnum.AWAITING_MOVIE_SELECTION);
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
        sessionService.setUserState(chatId, UserStateEnum.AWAITING_MOVIE_SELECTION);
    }
    @Transactional
    public void handleDeleteMovie(String chatId) {
        AppUser currentUser = sessionService.getCurrentUser(chatId);
        Movie selectedMovie = sessionService.getSelectedMovie(chatId);
        if (currentUser != null && selectedMovie != null) {
            userMovieService.updateMovieStatus(currentUser, selectedMovie, MovieStatus.WATCHED);
            messageSender.sendMessage(chatId, "Фильм успешно помечен как не просмотренный.");
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

        sessionService.clearUserState(chatId);
        messageSender.sendMainMenu(chatId);
    }



    public void handleRegisterCommand(String chatId, String username, String password) {
        try {
            AppUser newUser = new AppUser();
            newUser.setUsername(username);
            newUser.setPassword(password);
            appUserService.registerUser(newUser);
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
                messageSender.sendMessage(chatId, "Вы успешно вошли как " + username + ".");
                messageSender.sendMainMenu(chatId);
            } else {
                messageSender.sendMessage(chatId, "Неверный пароль. Попробуйте снова.");
            }
        } catch (IllegalArgumentException e) {
            messageSender.sendMessage(chatId, "Ошибка входа: " + e.getMessage());
        }
    }



    // Обработка команды добавления фильма
    @Transactional
    public void handleAddMovieCommand(String chatId, String messageText) {
        AppUser currentUser = sessionService.getCurrentUser(chatId);
        if (currentUser != null) {
            // Логирование для отладки
            logger.info("Пользователь {} начал процесс добавления фильма", currentUser.getUsername());

            // Отправка запроса в OMDb API для поиска фильма по названию
            List<Movie> foundMovies = omdbService.searchMoviesByTitle(messageText);

            if (foundMovies.isEmpty()) {
                messageSender.sendMessage(chatId, "Фильмы по запросу \"" + messageText + "\" не найдены.");
            } else {
                // Отправляем пользователю кнопки с найденными фильмами для выбора
                messageSender.sendMovieSelectionMessage(chatId, foundMovies);
                // Сохраняем состояние ожидания выбора фильма
                sessionService.setUserState(chatId, UserStateEnum.AWAITING_MOVIE_SELECTION);
            }
        } else {
            messageSender.sendMessage(chatId, "Вы не авторизованы. Используйте /login для входа.");
        }
    }



    public void handleFriendsCommand(String chatId) {
        AppUser currentUser = sessionService.getCurrentUser(chatId);
        if (currentUser != null) {
            messageSender.sendFriendsMenu(chatId, friendshipService.getFriends(currentUser.getUsername()));
        } else {
            messageSender.sendMessage(chatId, "Вы не авторизованы. Используйте /login для входа.");
        }
    }

    // Обработка команды добавления друга
    public void handleAddFriendCommand(String chatId, String messageText) {
        // Сообщаем пользователю, что нужно ввести имя друга
        messageSender.sendMessage(chatId, "Введите имя друга для добавления:");
        // Устанавливаем состояние ожидания имени друга
        sessionService.setUserState(chatId, UserStateEnum.WAITING_FOR_FRIEND_USERNAME);
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
            List<Movie> watchedMovies = appUserService.getWatchedMoviesByUser(currentUser.getId());
            if (watchedMovies.isEmpty()) {
                messageSender.sendMessage(chatId, "У вас нет просмотренных фильмов.");
            } else {
                // Отправляем актуальный список
                showWatchedMoviesList(chatId, watchedMovies);
                sessionService.setUserState(chatId, UserStateEnum.AWAITING_MOVIE_SELECTION);
            }
        } else {
            messageSender.sendMessage(chatId, "Вы не авторизованы. Используйте /login для входа.");
        }
    }
    public void handleMainMenuCommand(String chatId) {
        messageSender.sendMainMenu(chatId);
    }

    public void handleFriendsMenu(String chatId) {
        AppUser currentUser = sessionService.getCurrentUser(chatId);
        if (currentUser != null) {
            messageSender.sendFriendsMenu(chatId, friendshipService.getFriends(currentUser.getUsername()));
        } else {
            messageSender.sendMessage(chatId, "Вы не авторизованы. Используйте /login для входа.");
        }
    }


    public void handleRemovePlannedMovieCommand(String chatId, String imdbId) {
        AppUser currentUser = sessionService.getCurrentUser(chatId);
        if (currentUser != null) {
            try {
                appUserService.removePlannedMovie(currentUser.getUsername(), imdbId);
                messageSender.sendMessage(chatId, "Фильм успешно удален из списка запланированных.");
            } catch (IllegalArgumentException e) {
                messageSender.sendMessage(chatId, e.getMessage());
            }
            // Возвращаем пользователя в главное меню после удаления
            sessionService.clearUserState(chatId);
            messageSender.sendMainMenu(chatId);
        } else {
            messageSender.sendMessage(chatId, "Вы не авторизованы. Используйте /login для входа.");
        }
    }

  /*  public void handleMovieSelection(String chatId, String movieId) {
        AppUser currentUser = sessionService.getCurrentUser(chatId);
        if (currentUser != null) {
            try {
                Movie movie = omdbService.getMovieByImdbId(movieId); // Получение фильма по ID
                if (movie != null) {
                    appUserService.addWatchedMovie(currentUser.getUsername(), movie);
                    messageSender.sendMessage(chatId, "Фильм " + movie.getTitle() + " добавлен в ваш список просмотренных.");
                } else {
                    messageSender.sendMessage(chatId, "Фильм не найден.");
                }
            } catch (IllegalArgumentException e) {
                messageSender.sendMessage(chatId, "Ошибка: " + e.getMessage());
            }
        } else {
            messageSender.sendMessage(chatId, "Вы не авторизованы. Используйте /login для входа.");
        }
    }

   */

    @CacheEvict(value = "moviesCache", allEntries = true)
    public void clearAllCaches() {
        logger.info("Все кэши очищены!");
    }
}
