package kz.leansolutions.telegram_task_manager_bot.telegram.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Service
public class TelegramBotService extends TelegramLongPollingBot {
    @Value("${telegram.username}")
    private String botUsername;

    @Value("${telegram.apiToken}")
    private String apiToken;

    @Autowired
    private TelegramMessageReceiver telegramMessageReceiver;

    @Override
    public void onUpdateReceived(Update update) {
        telegramMessageReceiver.handleUpdate(update);
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return apiToken;
    }

    public void clearInlineKeyboard(Message message) {
        EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup();
        editMessageReplyMarkup.setChatId(message.getChatId().toString());
        editMessageReplyMarkup.setMessageId(message.getMessageId());
        editMessageReplyMarkup.setReplyMarkup(null);

        try {
            execute(editMessageReplyMarkup);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
