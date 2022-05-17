package kz.leansolutions.telegram_task_manager_bot.repository;

import kz.leansolutions.telegram_task_manager_bot.model.BotUser;
import kz.leansolutions.telegram_task_manager_bot.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BotUserRepo extends MongoRepository<BotUser, String> {
    Optional<BotUser> findByUser(User user);
}
