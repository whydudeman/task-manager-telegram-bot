package kz.leansolutions.telegram_task_manager_bot.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.time.LocalDateTime;

@Document("tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Task {
    @Id
    @EqualsAndHashCode.Include
    private String id;
    private String name;
    private String description;
    private String projectName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime deadline;
    private Status status = Status.IN_PROGRESS;
    private Priority priority = Priority.LOW;
    @DocumentReference
    private User manager;
    @DocumentReference
    private User executor;
    private Integer difficulty = 0;
}
