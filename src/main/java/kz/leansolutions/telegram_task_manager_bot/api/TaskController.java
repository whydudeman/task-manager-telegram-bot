package kz.leansolutions.telegram_task_manager_bot.api;

import kz.leansolutions.telegram_task_manager_bot.dto.IdResponse;
import kz.leansolutions.telegram_task_manager_bot.dto.TaskRequest;
import kz.leansolutions.telegram_task_manager_bot.model.Task;
import kz.leansolutions.telegram_task_manager_bot.model.User;
import kz.leansolutions.telegram_task_manager_bot.service.TaskService;
import kz.leansolutions.telegram_task_manager_bot.service.UserService;
import kz.leansolutions.telegram_task_manager_bot.telegram.model.NotificationType;
import kz.leansolutions.telegram_task_manager_bot.telegram.service.NotificationHandler;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/trigger")
@AllArgsConstructor
public class TaskController {
    private final TaskService taskService;
    private final NotificationHandler notificationHandler;
    private final UserService userService;

    @PostMapping("/send-all-tasks/{userId}")
    public ResponseEntity<Void> sendAllTasksWithPining(@PathVariable String userId) {
        User user = userService.getByIdWithThrow(userId);
        notificationHandler.handleExecutorTasksNotification(user);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/add-new-task")
    public ResponseEntity<IdResponse> addNewTask(@RequestBody TaskRequest taskRequest) {
        Task task = taskService.create(taskRequest);
        notificationHandler.handleOneTaskNotification(task, NotificationType.TASK_CREATED);
        //sendMessage
        return new ResponseEntity<>(IdResponse.get(task.getId()), HttpStatus.OK);
    }

    @PutMapping("/update-task/{id}")
    public ResponseEntity<IdResponse> updateTask(@PathVariable Long id,
                                                 @RequestBody @Valid TaskRequest taskRequest) {
        Task task = taskService.getByIdWithThrow(id);
        Task updated = taskService.update(task, taskRequest);
        notificationHandler.handleOneTaskNotification(updated, NotificationType.TASK_UPDATED);
        //sendMessage
        return new ResponseEntity<>(IdResponse.get(task.getId()), HttpStatus.OK);
    }
}
