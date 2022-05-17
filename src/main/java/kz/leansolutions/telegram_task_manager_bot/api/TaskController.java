package kz.leansolutions.telegram_task_manager_bot.api;

import kz.leansolutions.telegram_task_manager_bot.bot.TaskManagerBot;
import kz.leansolutions.telegram_task_manager_bot.dto.TaskRequest;
import kz.leansolutions.telegram_task_manager_bot.model.Task;
import kz.leansolutions.telegram_task_manager_bot.model.User;
import kz.leansolutions.telegram_task_manager_bot.service.TaskService;
import kz.leansolutions.telegram_task_manager_bot.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/trigger")
public class TaskController {
    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskManagerBot taskManagerBot;

    @Autowired
    private UserService userService;

    @PostMapping("/send-all-tasks/{userId}")
    public ResponseEntity<Void> sendAllTasksWithPining(@PathVariable String userId) {
        User user = userService.getByIdWithThrow(userId);
        taskManagerBot.generatePageableTaskMsg(user);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/add-new-task")
    public ResponseEntity<Void> addNewTask(@RequestBody TaskRequest taskRequest) {
        Task task = taskService.create(taskRequest);
        User user = userService.getByIdWithThrow(taskRequest.getExecutorId());
        taskManagerBot.sendNewOneTask(task, user);
        //sendMessage
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("/update-task/{id}")
    public ResponseEntity<Void> updateTask(@PathVariable String id,
                                           @RequestBody TaskRequest taskRequest) {
        Task task = taskService.getByIdWithThrow(id);
        Task updated = taskService.update(task, taskRequest);
        taskManagerBot.sendUpdatedTask(updated);
        //sendMessage
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
