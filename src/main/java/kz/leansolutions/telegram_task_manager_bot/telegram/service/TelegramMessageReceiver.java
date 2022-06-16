package kz.leansolutions.telegram_task_manager_bot.telegram.service;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface TelegramMessageReceiver {
    void handleUpdate(Update update);
}
