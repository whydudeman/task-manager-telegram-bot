package kz.leansolutions.telegram_task_manager_bot.telegram.service.impl;

import kz.leansolutions.telegram_task_manager_bot.model.User;
import kz.leansolutions.telegram_task_manager_bot.telegram.model.BotUser;
import kz.leansolutions.telegram_task_manager_bot.telegram.repository.BotUserRepo;
import kz.leansolutions.telegram_task_manager_bot.telegram.service.BotUserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class BotUserServiceImpl implements BotUserService {
    private final BotUserRepo botUserRepo;

    public BotUser getByChatId(String chatId) {
        return botUserRepo.findById(chatId).orElse(null);
    }

    public BotUser getByUser(User user) {
        return botUserRepo.findByUser(user).orElse(null);
    }

    public void save(BotUser botUser) {
        botUserRepo.save(botUser);
    }

    @Override
    public void deleteById(String chatId) {
        botUserRepo.deleteById(chatId);
    }
}
