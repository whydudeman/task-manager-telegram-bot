package kz.leansolutions.telegram_task_manager_bot.service;

import kz.leansolutions.telegram_task_manager_bot.model.User;
import kz.leansolutions.telegram_task_manager_bot.repository.UserRepo;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class UserService {
    private final UserRepo userRepo;

    public User getByIdWithThrow(String id) {
        return userRepo.findById(id).orElseThrow(() -> new RuntimeException("ENTITY_DOES_NOT_EXISTS"));
    }

    public User getById(String id) {
        return userRepo.findById(id).orElse(null);
    }

    public Optional<User> getUserByPhone(String phone) {
        return userRepo.findByPhone(phone);
    }

    public void save(User user) {
        userRepo.save(user);
    }
}
