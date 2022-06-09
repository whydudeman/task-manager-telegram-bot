package kz.leansolutions.telegram_task_manager_bot.service;

import kz.leansolutions.telegram_task_manager_bot.model.BotUser;
import kz.leansolutions.telegram_task_manager_bot.model.User;
import kz.leansolutions.telegram_task_manager_bot.repository.BotUserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BotUserService {
    @Autowired
    private BotUserRepo botUserRepo;

    public BotUser getByIdOrNew(String chatId) {
        BotUser newBotUser = new BotUser();
        newBotUser.setChatId(chatId);

        return botUserRepo.findById(chatId).orElse(newBotUser);
    }

    public BotUser getByIdOrThrow(String chatId) {
        BotUser newBotUser = new BotUser();
        newBotUser.setChatId(chatId);

        return botUserRepo.findById(chatId).orElseThrow(() -> new RuntimeException("ENTITY_DOES_NOT_EXISTS"));
    }

    public BotUser getByUser(User user) {
        return botUserRepo.findByUser(user).orElse(null);
    }

    public void deleteById(String id) {
        botUserRepo.deleteById(id);
    }

    public void save(BotUser botUser) {
        botUserRepo.save(botUser);
    }
}
