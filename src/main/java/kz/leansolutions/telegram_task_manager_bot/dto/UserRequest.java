package kz.leansolutions.telegram_task_manager_bot.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRequest {
    private String id;

    private String name;

    private String username;

    private String password;

    private String phone;
}
