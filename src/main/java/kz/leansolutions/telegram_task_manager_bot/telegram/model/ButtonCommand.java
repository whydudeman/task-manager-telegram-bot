package kz.leansolutions.telegram_task_manager_bot.telegram.model;

import java.util.HashMap;
import java.util.Map;

public enum ButtonCommand {
    YES("yes", "Да"),
    NO("no", "Нет"),
    PAGE("page", "");

    private static final Map<String, ButtonCommand> values;

    static {
        values = new HashMap<>();

        for (ButtonCommand command : ButtonCommand.values()) {
            values.put(command.getValue(), command);
        }
    }

    private final String value;
    private final String description;


    ButtonCommand(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public static ButtonCommand getByValue(String value) {
        return values.get(value);
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }
}
