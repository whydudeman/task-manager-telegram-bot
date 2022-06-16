package kz.leansolutions.telegram_task_manager_bot.telegram.service;

import kz.leansolutions.telegram_task_manager_bot.model.Task;
import kz.leansolutions.telegram_task_manager_bot.model.User;
import kz.leansolutions.telegram_task_manager_bot.telegram.model.NotificationType;

public interface NotificationHandler {
    void handleOneTaskNotification(Task task, NotificationType notificationType);

    void handleExecutorTasksNotification(User executor);
}
