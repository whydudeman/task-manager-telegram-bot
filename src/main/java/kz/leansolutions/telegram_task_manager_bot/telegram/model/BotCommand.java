package kz.leansolutions.telegram_task_manager_bot.telegram.model;

import java.util.HashMap;
import java.util.Map;

public enum BotCommand {
    START("/start"),
    FINISH_TASK("/finish_task_"),
    GET_TASKS("/tasks"),
    REGISTRATION("/registration");

    private String value;
    private static final Map<String, BotCommand> values;

    static {
        values = new HashMap<>();

        for (BotCommand command : BotCommand.values()) {
            values.put(command.getValue(), command);
        }
    }

    BotCommand(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }


    public static BotCommand getByValue(String value) {
        if (value != null && !value.isEmpty() && value.startsWith(FINISH_TASK.getValue()))
            return FINISH_TASK;
        return values.get(value);
    }
}
