package kz.leansolutions.telegram_task_manager_bot.repository;

import kz.leansolutions.telegram_task_manager_bot.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepo extends MongoRepository<User, String> {
    Optional<User> findByPhone(String phone);
}
