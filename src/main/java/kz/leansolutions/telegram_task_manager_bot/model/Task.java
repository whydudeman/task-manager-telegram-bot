package kz.leansolutions.telegram_task_manager_bot.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Document("tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Task {
    @Transient
    public static final String SEQUENCE_NAME = "task_sequence";
    @Id
    @EqualsAndHashCode.Include
    private Long id; //chang it to only digital id

    @NotNull
    private String name;
    @NotNull
    private String description;
    @NotNull
    private String projectName;
    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime deadline;
    private Status status = Status.IN_PROGRESS;
    private Priority priority = Priority.LOW;
    @NotNull
    @DocumentReference
    private User manager;
    @NotNull
    @DocumentReference
    private User executor;
    private Integer difficulty = 0;
}
