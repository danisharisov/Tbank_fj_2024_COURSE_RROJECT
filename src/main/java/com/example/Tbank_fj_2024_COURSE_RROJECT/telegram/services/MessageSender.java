package com.example.Tbank_fj_2024_COURSE_RROJECT.telegram.services;

import com.example.Tbank_fj_2024_COURSE_RROJECT.models.movie.Movie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

import java.util.ArrayList;
import java.util.List;

@Component
public class MessageSender {

    private static final Logger logger = LoggerFactory.getLogger(MessageSender.class);

    private final TelegramLongPollingBot bot;

    @Autowired
    public MessageSender(@Lazy TelegramLongPollingBot bot) {
        this.bot = bot;
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

    public void sendMovieSelectionMessage(String chatId, List<Movie> movies) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        for (Movie movie : movies) {
            List<InlineKeyboardButton> rowInline = new ArrayList<>();
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(movie.getTitle() + " (" + movie.getYear() + ")");
            button.setCallbackData("movie:" + movie.getImdbId());
            rowInline.add(button);
            rowsInline.add(rowInline);
        }
        markupInline.setKeyboard(rowsInline);
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Выберите фильм:");
        message.setReplyMarkup(markupInline);
        try {
            bot.execute(message);
            logger.info("Movie selection message sent to chatId: {}", chatId);
        } catch (TelegramApiException e) {
            logger.error("Error sending movie selection message to chatId: {}", chatId, e);
        }
    }
}