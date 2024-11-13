// CommandHandler.java
package com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.handlers;

import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.command.friendship.AddFriendCommand;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.command.friendship.DeleteFriendCommand;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.command.movie.*;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.UserStateEnum;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.MessageSender;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.SessionService;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class CommandHandler {

    private final SessionService sessionService;

    private final  MessageSender messageSender;

    private final AddMovieCommand addMovieCommand;

    private final  UnloggedStateHandler unloggedStateHandler;

    private final PickWatchedMovieCommand pickWatchedMovieCommand;

    private final PickPlannedMovieCommand pickPlannedMovieCommand;

    private final RateMovieCommand rateMovieCommand;
    private final SetHypeCommand setHypeCommand;
    private final DeleteFriendCommand deleteFriendCommand;
    private final AddFriendCommand addFriendCommand;

    public CommandHandler(SessionService sessionService, MessageSender messageSender,
                          AddMovieCommand addMovieCommand, UnloggedStateHandler unloggedStateHandler,
                          PickWatchedMovieCommand pickWatchedMovieCommand, PickPlannedMovieCommand pickPlannedMovieCommand,
                          RateMovieCommand rateMovieCommand, SetHypeCommand setHypeCommand, DeleteFriendCommand deleteFriendCommand,
                          AddFriendCommand addFriendCommand) {
        this.sessionService = sessionService;
        this.messageSender = messageSender;
        this.addMovieCommand = addMovieCommand;
        this.unloggedStateHandler = unloggedStateHandler;
        this.pickWatchedMovieCommand = pickWatchedMovieCommand;
        this.pickPlannedMovieCommand = pickPlannedMovieCommand;
        this.rateMovieCommand = rateMovieCommand;
        this.setHypeCommand = setHypeCommand;
        this.deleteFriendCommand = deleteFriendCommand;
        this.addFriendCommand = addFriendCommand;
    }

    public void handleStateBasedCommand(String chatId, String messageText, UserStateEnum state) {
        switch (state) {
            case DEFAULT_UNLOGGED:
                unloggedStateHandler.handleUnloggedState(chatId, messageText);
                break;
            case WAITING_FOR_MOVIE_TITLE:
                addMovieCommand.execute(chatId, Collections.singletonList(messageText));
                break;
            case WAITING_WATCHED_MOVIE_NUMBER:
                pickWatchedMovieCommand.execute(chatId,Collections.singletonList(messageText));
                break;
            case WAITING_PLANNED_MOVIE_NUMBER:
                pickPlannedMovieCommand.execute(chatId,Collections.singletonList(messageText));
                break;
            case WAITING_MOVIE_RATING:
                rateMovieCommand.execute(chatId,Collections.singletonList(messageText));
                break;
            case WAITING_MOVIE_HYPE:
                setHypeCommand.execute(chatId, Collections.singletonList(messageText));
                break;
            case WAITING_FRIEND_DELETION:
                deleteFriendCommand.execute(chatId, Collections.singletonList(messageText));
                break;
            case WAITING_FOR_FRIEND_USERNAME:
                addFriendCommand.execute(chatId, Collections.singletonList(messageText));
                sessionService.clearUserState(chatId);
                break;
            default:
                messageSender.sendMessage(chatId, "Неизвестное состояние.");
                break;
        }
    }

}
