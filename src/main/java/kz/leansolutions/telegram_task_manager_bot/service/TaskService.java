package kz.leansolutions.telegram_task_manager_bot.service;

import kz.leansolutions.telegram_task_manager_bot.config.service.SequenceGeneratorService;
import kz.leansolutions.telegram_task_manager_bot.dto.TaskRequest;
import kz.leansolutions.telegram_task_manager_bot.model.Status;
import kz.leansolutions.telegram_task_manager_bot.model.Task;
import kz.leansolutions.telegram_task_manager_bot.model.User;
import kz.leansolutions.telegram_task_manager_bot.repository.TaskRepo;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@AllArgsConstructor
public class TaskService {
    private final TaskRepo taskRepo;
    private final UserService userService;
    private final SequenceGeneratorService sequenceGeneratorService;

    public List<Task> getAllByExecutor(User user) {
        return taskRepo.findAllByExecutor(user);
    }

    public Page<Task> getAllByExecutor(User user, Integer page, Integer size) {
        Pageable paging = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "deadline")
        );
        return taskRepo.findAllByExecutor(user, paging);
    }

    public Task getById(Long id) {
        return taskRepo.findById(id).orElse(null);
    }

    public Task getByIdWithThrow(Long id) {
        return taskRepo.findById(id).orElseThrow(() -> new RuntimeException("ENTITY_DOES_NOT_EXISTS"));
    }

    public Task save(Task task) {
        return taskRepo.save(task);
    }

    public void saveAll(List<Task> tasks) {
        taskRepo.saveAll(tasks);
    }

    public Task create(TaskRequest taskRequest) {
        User manager = userService.getById(taskRequest.getManagerId());
        User executor = userService.getById(taskRequest.getManagerId());

        Task task = Task.builder()
                .id(sequenceGeneratorService.generateSequence(Task.SEQUENCE_NAME))
                .name(taskRequest.getName())
                .description(taskRequest.getDescription())
                .projectName(taskRequest.getProjectName())
                .deadline(taskRequest.getDeadline())
                .status(taskRequest.getStatus())
                .priority(taskRequest.getPriority())
                .manager(manager)
                .executor(executor)
                .difficulty(taskRequest.getDifficulty())
                .build();

        return save(task);
    }

    public Task update(Task task, TaskRequest taskRequest) {
        User manager = userService.getById(taskRequest.getManagerId());
        User executor = userService.getById(taskRequest.getManagerId());

        Task updatedTask = task.toBuilder()
                .name(taskRequest.getName())
                .description(taskRequest.getDescription())
                .projectName(taskRequest.getProjectName())
                .deadline(taskRequest.getDeadline())
                .status(taskRequest.getStatus())
                .priority(taskRequest.getPriority())
                .manager(manager)
                .executor(executor)
                .difficulty(taskRequest.getDifficulty())
                .build();

        return save(updatedTask);
    }

    public List<Task> getTasksWithPassedDeadlineWithStatusInProgress() {
        return taskRepo
                .findByStatusNotInAndDeadlineNotNullAndDeadlineBefore(
                        Arrays.asList(Status.NOT_DONE, Status.DONE),
                        LocalDateTime.now());
    }
}
