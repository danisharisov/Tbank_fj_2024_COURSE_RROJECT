// CommandHandler.java
package com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.handlers;

import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.command.friendship.AddFriendCommand;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.command.friendship.DeleteFriendCommand;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.command.movie.*;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.UserStateEnum;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.MessageSender;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class CommandHandler {
    @Autowired
    private SessionService sessionService;
    @Autowired
    private  MessageSender messageSender;
    @Autowired
    private AddMovieCommand addMovieCommand;
    @Autowired
    private  UnloggedStateHandler unloggedStateHandler;
    @Autowired
    private PickWatchedMovieCommand pickWatchedMovieCommand;
    @Autowired
    private PickPlannedMovieCommand pickPlannedMovieCommand;
    @Autowired
    private RateMovieCommand rateMovieCommand;
    @Autowired
    private SetHypeCommand setHypeCommand;
    @Autowired
    private DeleteFriendCommand deleteFriendCommand;
    @Autowired
    private AddFriendCommand addFriendCommand;

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
          /*
            case AWAITING_PLANNED_MOVIE_SELECTION:
                handleMovieSelectionByIndex(chatId, messageText, true);
                break;
            case AWAITING_MOVIE_HYPE:
                processMovieHype(chatId, messageText);
                break;
            case AWAITING_MOVIE_RATING:
                processMovieRating(chatId, messageText);
                break;
            case DEFAULT_LOGGED:
                messageSender.sendMainMenu(chatId);
                break;*/
            default:
                messageSender.sendMessage(chatId, "Неизвестное состояние.");
                break;
        }
    }


}
