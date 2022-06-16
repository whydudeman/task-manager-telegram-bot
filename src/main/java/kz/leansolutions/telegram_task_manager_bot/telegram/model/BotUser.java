package kz.leansolutions.telegram_task_manager_bot.telegram.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import kz.leansolutions.telegram_task_manager_bot.model.User;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

@Document("bot_users")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class BotUser {
    @Id
    @EqualsAndHashCode.Include
    private String chatId;
    private Integer lastPinnedMessageId;
    private BotState currentState;
    @JsonIgnore
    @DocumentReference
    private User user;
}
