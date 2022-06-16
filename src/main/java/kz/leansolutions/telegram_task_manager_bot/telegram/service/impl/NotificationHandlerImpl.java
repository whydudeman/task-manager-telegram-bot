package kz.leansolutions.telegram_task_manager_bot.telegram.service.impl;

import kz.leansolutions.telegram_task_manager_bot.model.Task;
import kz.leansolutions.telegram_task_manager_bot.model.User;
import kz.leansolutions.telegram_task_manager_bot.service.TaskService;
import kz.leansolutions.telegram_task_manager_bot.telegram.model.BotUser;
import kz.leansolutions.telegram_task_manager_bot.telegram.model.NotificationType;
import kz.leansolutions.telegram_task_manager_bot.telegram.service.NotificationHandler;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class NotificationHandlerImpl implements NotificationHandler {
    private final TelegramNotificationService telegramNotificationService;
    private final TaskService taskService;

    @Override
    public void handleOneTaskNotification(Task task, NotificationType notificationType) {
        if (notificationType != null && task != null && task.getExecutor() != null) {
            BotUser botUser = task.getExecutor().getBotUser();
            if (botUser != null)
                telegramNotificationService.sendNotification(task, notificationType, botUser.getChatId());
        }
    }

    @Override
    public void handleExecutorTasksNotification(User executor) {
        if (executor != null) {
            BotUser botUser = executor.getBotUser();
            Page<Task> tasks = taskService.getAllByExecutor(executor, 0, 3);
            if (botUser != null)
                telegramNotificationService.sendAllTaskNotification(tasks, NotificationType.TASKS_PIN, botUser.getChatId());
        }
    }
}
