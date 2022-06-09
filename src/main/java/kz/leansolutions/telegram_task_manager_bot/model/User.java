package kz.leansolutions.telegram_task_manager_bot.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

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

    private String name;

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
