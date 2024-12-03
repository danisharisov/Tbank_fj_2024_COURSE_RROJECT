package com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.handlers;

import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.command.Command;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.command.friendship.AddFriendCommand;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.command.friendship.DeleteFriendCommand;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.command.movie.*;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.MessageSender;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.SessionService;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.UserStateEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.mockito.Mockito.*;

class CommandHandlerTest {

    @Mock
    private SessionService sessionService;

    @Mock
    private MessageSender messageSender;

    @Mock
    private AddMovieCommand addMovieCommand;

    @Mock
    private PickWatchedMovieCommand pickWatchedMovieCommand;

    @Mock
    private PickPlannedMovieCommand pickPlannedMovieCommand;

    @Mock
    private RateMovieCommand rateMovieCommand;

    @Mock
    private SetHypeCommand setHypeCommand;

    @Mock
    private DeleteFriendCommand deleteFriendCommand;

    @Mock
    private AddFriendCommand addFriendCommand;

    @Mock
    private UnloggedStateHandler unloggedStateHandler;

    private CommandHandler commandHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        commandHandler = new CommandHandler(
                sessionService, messageSender, addMovieCommand, unloggedStateHandler,
                pickWatchedMovieCommand, pickPlannedMovieCommand, rateMovieCommand,
                setHypeCommand, deleteFriendCommand, addFriendCommand
        );
        commandHandler.initCommandMap();
    }

    // Проверяет обработку состояния DEFAULT_UNLOGGED
    @Test
    void handleStateBasedCommand_DefaultUnlogged() {
        String chatId = "12345";
        String messageText = "Test message";
        String username = "testUser";

        commandHandler.handleStateBasedCommand(chatId, messageText, UserStateEnum.DEFAULT_UNLOGGED, username);

        verify(unloggedStateHandler, times(1)).handleUnloggedState(chatId, messageText, username);
        verifyNoInteractions(addMovieCommand, pickWatchedMovieCommand, messageSender);
    }

    // Проверяет обработку состояния WAITING_FOR_MOVIE_TITLE
    @Test
    void handleStateBasedCommand_RegisteredState() {
        String chatId = "12345";
        String messageText = "Test movie";
        UserStateEnum state = UserStateEnum.WAITING_FOR_MOVIE_TITLE;

        commandHandler.handleStateBasedCommand(chatId, messageText, state, null);

        verify(addMovieCommand, times(1)).execute(chatId, List.of(messageText));
        verifyNoInteractions(messageSender);
    }

    // Проверяет обработку неизвестного состояния
    @Test
    void handleStateBasedCommand_UnknownState() {
        String chatId = "12345";
        String messageText = "Unknown command";
        UserStateEnum state = UserStateEnum.DEFAULT_LOGGED; // Не зарегистрированное состояние

        commandHandler.handleStateBasedCommand(chatId, messageText, state, null);

        verify(messageSender, times(1)).sendMessage(chatId, "Неизвестное состояние.");
        verifyNoInteractions(addMovieCommand, pickWatchedMovieCommand);
    }
}
