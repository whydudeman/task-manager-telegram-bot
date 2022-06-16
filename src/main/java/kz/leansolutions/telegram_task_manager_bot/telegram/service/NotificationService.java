package kz.leansolutions.telegram_task_manager_bot.telegram.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import kz.leansolutions.telegram_task_manager_bot.model.Task;
import kz.leansolutions.telegram_task_manager_bot.telegram.model.BotLanguage;
import kz.leansolutions.telegram_task_manager_bot.telegram.model.NotificationType;
import org.springframework.data.domain.Page;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public abstract class NotificationService {
    protected Map<BotLanguage, Map<String, String>> messages;

    protected NotificationService() {
    }

    protected NotificationService(Map<BotLanguage, String> messagesPaths) {
        messages = new HashMap<>();
        messagesPaths.forEach((language, messagesPath) -> {
            ObjectMapper objectMapper = new ObjectMapper();
            InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(messagesPath);
            if (inputStream != null)
                try {
                    messages.put(language, objectMapper.readValue(inputStream, HashMap.class));
                } catch (IOException e) {
                    e.printStackTrace();
                }
        });
    }

    public abstract void sendNotification(Task task, NotificationType notificationType, String receiverChatId);

    public abstract void sendAllTaskNotification(Page<Task> tasks, NotificationType notificationType, String chatId);

    public abstract void sendAllTaskNotification(Page<Task> tasks, NotificationType notificationType, String chatId, Integer lastPinnedMessageId);

    protected String getMessage(NotificationType notificationType, Map<String, String> values, BotLanguage language) {
        Map<String, String> messageValues = messages.get(language);
        String result = (messageValues != null) ? messageValues.getOrDefault(notificationType.toString(), "") : "";
        return setPlaceholders(result, values);
    }

    protected String getMessage(NotificationType.AdditionalType notificationAdditionalType, Map<String, String> values, BotLanguage language) {
        Map<String, String> messageValues = messages.get(language);
        String result = (messageValues != null) ? messageValues.getOrDefault(notificationAdditionalType.toString(), "") : "";
        return setPlaceholders(result, values);
    }

    protected String setPlaceholders(String text, Map<String, String> values) {
        String result = text;
        for (Map.Entry<String, String> entry : values.entrySet()) {
            String value = (entry.getValue() != null) ? entry.getValue() : "";
            result = result.replaceAll("\\{\\{" + entry.getKey() + "\\}\\}", value);
        }

        return result;
    }
}
