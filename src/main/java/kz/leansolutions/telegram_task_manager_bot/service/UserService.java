package kz.leansolutions.telegram_task_manager_bot.service;

import kz.leansolutions.telegram_task_manager_bot.dto.UserRequest;
import kz.leansolutions.telegram_task_manager_bot.model.User;
import kz.leansolutions.telegram_task_manager_bot.repository.UserRepo;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class UserService {
    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;

    public List<User> getAll() {
        return userRepo.findAll();
    }

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

    public User create(UserRequest userRequest) {
        return User.builder()
                .id(userRequest.getId())
                .name(userRequest.getName())
                .username(userRequest.getUsername())
                .password(Optional.ofNullable(userRequest.getPassword()).map(passwordEncoder::encode).orElse(null))
                .phone(userRequest.getPhone())
                .build();
    }

    public User update(User user, UserRequest userRequest) {
        return user.toBuilder()
                .id(userRequest.getId())
                .name(userRequest.getName())
                .username(userRequest.getUsername())
                .password(Optional.ofNullable(userRequest.getPassword()).map(passwordEncoder::encode).orElse(null))
                .phone(userRequest.getPhone())
                .build();
    }
}
