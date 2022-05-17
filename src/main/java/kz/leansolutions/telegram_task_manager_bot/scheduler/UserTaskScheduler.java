package kz.leansolutions.telegram_task_manager_bot.scheduler;

import kz.leansolutions.telegram_task_manager_bot.bot.TaskManagerBot;
import kz.leansolutions.telegram_task_manager_bot.model.Status;
import kz.leansolutions.telegram_task_manager_bot.model.Task;
import kz.leansolutions.telegram_task_manager_bot.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class UserTaskScheduler {
    @Autowired
    private TaskService taskService;
    @Autowired
    private TaskManagerBot taskManagerBot;

    @Scheduled(fixedDelay = 60_000)
    public void checkDeadlinePassed() {
        log.warn("checkDeadlinePassed");
        List<Task> allWithDeadlineTodayAndStatusIsNotDone = taskService
                .getAllWithDeadlineTodayAndStatusIsNotDone();
        log.warn("Size {}", allWithDeadlineTodayAndStatusIsNotDone.size());
        allWithDeadlineTodayAndStatusIsNotDone
                .forEach(t -> {
                    taskManagerBot.sendPassedDeadlineTask(t);
                    t.setStatus(Status.NOT_DONE);
                    taskService.save(t);
                });
    }
}
