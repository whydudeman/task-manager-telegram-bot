package kz.leansolutions.telegram_task_manager_bot.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import kz.leansolutions.telegram_task_manager_bot.telegram.model.BotUser;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import javax.validation.constraints.NotNull;
import java.util.List;

@Document("users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User {
    @Id
    @EqualsAndHashCode.Include
    private String id;

    @NotNull
    private String name;

    @NotNull
    private String username;

    @JsonIgnore
    private String password;

    private String phone;

    @JsonIgnore
    @DocumentReference(lookup = "{'executor':?#{#self._id} }")
    private List<Task> tasks;

    @DocumentReference(lookup = "{'user':?#{#self._id} }")
    private BotUser botUser;
}
