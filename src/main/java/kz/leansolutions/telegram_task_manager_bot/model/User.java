package kz.leansolutions.telegram_task_manager_bot.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.util.List;

@Document("users")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User {
    @Id
    @EqualsAndHashCode.Include
    private String id;

    private String name;

    private String username;

    private String password;

    private String phone;

    @ReadOnlyProperty
    @DocumentReference(lookup = "{'executor':?#{#self._id} }")
    private List<Task> tasks;

    @DocumentReference(lookup = "{'user':?#{#self._id} }")
    private BotUser botUser;
}
