package kz.leansolutions.telegram_task_manager_bot.telegram.controller;

import kz.leansolutions.telegram_task_manager_bot.telegram.model.BotUser;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface TelegramReceiverController {
    void handleStart(String chatId);

    void handleRegistrationCommand(String chatId);

    void handleRegistrationAnswer(BotUser bot, Message message);

    void handlePhoneAnswer(BotUser bot, Message message);

    void handleFinishTask(String chatId, String taskId);

    void handlePageChange(String chatId, String pageNumber);
}
