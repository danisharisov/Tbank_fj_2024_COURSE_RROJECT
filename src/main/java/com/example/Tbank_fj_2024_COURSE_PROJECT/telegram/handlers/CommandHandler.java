package com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.handlers;

import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.command.Command;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.command.friendship.*;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.command.movie.*;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.MessageSender;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.SessionService;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.UserStateEnum;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

@Component
public class CommandHandler {

    private final SessionService sessionService;
    private final MessageSender messageSender;

    private final AddMovieCommand addMovieCommand;
    private final PickWatchedMovieCommand pickWatchedMovieCommand;
    private final PickPlannedMovieCommand pickPlannedMovieCommand;
    private final RateMovieCommand rateMovieCommand;
    private final SetHypeCommand setHypeCommand;
    private final DeleteFriendCommand deleteFriendCommand;
    private final AddFriendCommand addFriendCommand;

    private final UnloggedStateHandler unloggedStateHandler;

    private final Map<UserStateEnum, Command> commandMap = new EnumMap<>(UserStateEnum.class);

    public CommandHandler(SessionService sessionService, MessageSender messageSender,
                          AddMovieCommand addMovieCommand, UnloggedStateHandler unloggedStateHandler,
                          PickWatchedMovieCommand pickWatchedMovieCommand, PickPlannedMovieCommand pickPlannedMovieCommand,
                          RateMovieCommand rateMovieCommand, SetHypeCommand setHypeCommand,
                          DeleteFriendCommand deleteFriendCommand, AddFriendCommand addFriendCommand) {
        this.sessionService = sessionService;
        this.messageSender = messageSender;
        this.addMovieCommand = addMovieCommand;
        this.pickWatchedMovieCommand = pickWatchedMovieCommand;
        this.pickPlannedMovieCommand = pickPlannedMovieCommand;
        this.rateMovieCommand = rateMovieCommand;
        this.setHypeCommand = setHypeCommand;
        this.deleteFriendCommand = deleteFriendCommand;
        this.addFriendCommand = addFriendCommand;
        this.unloggedStateHandler = unloggedStateHandler;
    }

    @PostConstruct
    private void initCommandMap() {
        commandMap.put(UserStateEnum.WAITING_FOR_MOVIE_TITLE, addMovieCommand);
        commandMap.put(UserStateEnum.WAITING_WATCHED_MOVIE_NUMBER, pickWatchedMovieCommand);
        commandMap.put(UserStateEnum.WAITING_PLANNED_MOVIE_NUMBER, pickPlannedMovieCommand);
        commandMap.put(UserStateEnum.WAITING_MOVIE_RATING, rateMovieCommand);
        commandMap.put(UserStateEnum.WAITING_MOVIE_HYPE, setHypeCommand);
        commandMap.put(UserStateEnum.WAITING_FRIEND_DELETION, deleteFriendCommand);
        commandMap.put(UserStateEnum.WAITING_FOR_FRIEND_USERNAME, addFriendCommand);
    }

    public void handleStateBasedCommand(String chatId, String messageText, UserStateEnum state, String username) {
        if (state == UserStateEnum.DEFAULT_UNLOGGED) {
            unloggedStateHandler.handleUnloggedState(chatId, messageText, username);
            return;
        }

        Command command = commandMap.get(state);
        if (command != null) {
            command.execute(chatId, Collections.singletonList(messageText));
        } else {
            messageSender.sendMessage(chatId, "Неизвестное состояние.");
        }
    }

}
