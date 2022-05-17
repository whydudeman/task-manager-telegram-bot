package kz.leansolutions.telegram_task_manager_bot.scheduler;

import kz.leansolutions.telegram_task_manager_bot.model.Task;
import kz.leansolutions.telegram_task_manager_bot.model.User;
import kz.leansolutions.telegram_task_manager_bot.service.TaskService;
import kz.leansolutions.telegram_task_manager_bot.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class InsertScheduler {
    @Autowired
    private TaskService taskService;

    @Autowired
    private UserService userService;

    @Scheduled(fixedDelay = 60_000)
    public void checkDeadlinePassed() {
        userService.save(new User());
        taskService.save(new Task());
    }


}
