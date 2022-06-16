package kz.leansolutions.telegram_task_manager_bot.telegram.service;

import kz.leansolutions.telegram_task_manager_bot.model.User;
import kz.leansolutions.telegram_task_manager_bot.telegram.model.BotUser;

public interface BotUserService {
    BotUser getByChatId(String chatId);

    BotUser getByUser(User user);

    void save(BotUser botUser);

    void deleteById(String chatId);
}
