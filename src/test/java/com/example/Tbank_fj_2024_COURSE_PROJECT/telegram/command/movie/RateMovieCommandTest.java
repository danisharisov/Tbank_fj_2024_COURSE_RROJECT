package com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.command.movie;

import com.example.Tbank_fj_2024_COURSE_PROJECT.services.UserMovieService;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.user.AppUser;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.movie.Movie;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.MessageSender;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.SessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.mockito.Mockito.*;

class RateMovieCommandTest {

    @Mock
    private SessionService sessionService;

    @Mock
    private UserMovieService userMovieService;

    @Mock
    private MessageSender messageSender;

    @InjectMocks
    private RateMovieCommand rateMovieCommand;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(sessionService.getCurrentUser("12345")).thenReturn(new AppUser()); // Мок авторизованного пользователя
    }

    // Успешное добавление оценки фильму
    @Test
    void execute_AddRatingSuccess() {
        String chatId = "12345";
        AppUser user = new AppUser();
        user.setUsername("testUser");

        Movie movie = new Movie();
        movie.setImdbId("tt1234567");

        when(sessionService.getSelectedMovie(chatId)).thenReturn(movie);
        when(sessionService.getCurrentUser(chatId)).thenReturn(user);

        rateMovieCommand.execute(chatId, List.of("8.5"));

        verify(userMovieService).addRating("testUser", "tt1234567", 8.5);
        verify(messageSender).sendMessage(chatId, "Оценка успешно добавлена.");
        verify(messageSender).sendMainMenu(chatId);
    }

    // Ошибка: фильм не выбран
    @Test
    void execute_NoMovieSelected() {
        String chatId = "12345";

        when(sessionService.getSelectedMovie(chatId)).thenReturn(null);

        rateMovieCommand.execute(chatId, List.of("8.5"));

        verify(messageSender).sendMessage(chatId, "Ошибка: выберите фильм для оценки.");
        verifyNoInteractions(userMovieService);
    }

    // Ошибка: оценка не указана
    @Test
    void execute_NoRatingProvided() {
        String chatId = "12345";

        Movie movie = new Movie();
        when(sessionService.getSelectedMovie(chatId)).thenReturn(movie);

        rateMovieCommand.execute(chatId, List.of());

        verify(messageSender).sendMessage(chatId, "Введите оценку от 0 до 10.0.");
        verifyNoInteractions(userMovieService);
    }

    // Ошибка: некорректный формат оценки
    @Test
    void execute_InvalidRatingFormat() {
        String chatId = "12345";

        Movie movie = new Movie();
        when(sessionService.getSelectedMovie(chatId)).thenReturn(movie);

        rateMovieCommand.execute(chatId, List.of("invalid"));

        verify(messageSender).sendMessage(chatId, "Некорректный формат числа. Введите оценку от 0 до 10.");
        verifyNoInteractions(userMovieService);
    }

    // Ошибка: некорректное значение оценки
    @Test
    void execute_InvalidRatingValue() {
        String chatId = "12345";

        Movie movie = new Movie();
        when(sessionService.getSelectedMovie(chatId)).thenReturn(movie);

        rateMovieCommand.execute(chatId, List.of("11"));

        verify(messageSender).sendMessage(chatId, "Некорректное значение. Введите оценку от 0 до 10.");
        verifyNoInteractions(userMovieService);
    }
}
