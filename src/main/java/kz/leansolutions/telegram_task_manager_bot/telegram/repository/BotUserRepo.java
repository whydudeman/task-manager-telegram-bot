package kz.leansolutions.telegram_task_manager_bot.telegram.repository;

import kz.leansolutions.telegram_task_manager_bot.model.User;
import kz.leansolutions.telegram_task_manager_bot.telegram.model.BotUser;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BotUserRepo extends MongoRepository<BotUser, String> {
    Optional<BotUser> findByUser(User user);
}
