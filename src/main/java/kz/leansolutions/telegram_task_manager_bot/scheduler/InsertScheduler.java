package kz.leansolutions.telegram_task_manager_bot.scheduler;

import kz.leansolutions.telegram_task_manager_bot.service.TaskService;
import kz.leansolutions.telegram_task_manager_bot.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InsertScheduler {
    @Autowired
    private TaskService taskService;

    @Autowired
    private UserService userService;
    //TODO inserter for initial data
}
