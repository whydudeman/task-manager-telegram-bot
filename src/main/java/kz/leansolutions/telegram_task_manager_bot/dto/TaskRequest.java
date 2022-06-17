package kz.leansolutions.telegram_task_manager_bot.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import kz.leansolutions.telegram_task_manager_bot.model.Priority;
import kz.leansolutions.telegram_task_manager_bot.model.Status;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@Setter
public class TaskRequest {
    private String name;
    private String description;
    private String projectName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime deadline;
    private Status status;
    private Priority priority;
    private String managerId;
    @NotNull
    private String executorId;
    private Integer difficulty;
}
