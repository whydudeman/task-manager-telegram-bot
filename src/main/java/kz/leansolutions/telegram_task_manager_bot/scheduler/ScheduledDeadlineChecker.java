package kz.leansolutions.telegram_task_manager_bot.scheduler;

import kz.leansolutions.telegram_task_manager_bot.model.Status;
import kz.leansolutions.telegram_task_manager_bot.model.Task;
import kz.leansolutions.telegram_task_manager_bot.service.TaskService;
import kz.leansolutions.telegram_task_manager_bot.telegram.model.NotificationType;
import kz.leansolutions.telegram_task_manager_bot.telegram.service.NotificationHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class ScheduledDeadlineChecker {
    @Autowired
    private TaskService taskService;
    @Autowired
    private NotificationHandler notificationHandler;

    @Transactional
    @Scheduled(cron = "${cron.checkDeadline}")
    public void checkDeadlinePassed() {
        List<Task> tasksWithPassedDeadline = taskService.getTasksWithPassedDeadlineWithStatusInProgress();
        tasksWithPassedDeadline.forEach(t -> {
            notificationHandler.handleOneTaskNotification(t, NotificationType.TASK_DEADLINE_PASSED);
            t.setStatus(Status.NOT_DONE);
            taskService.save(t);
        });
    }
}
