package kz.leansolutions.telegram_task_manager_bot.model;

public enum Priority {
    HIGH("Высокий"),
    MEDIUM("Средний"),
    LOW("Низкий");

    private String name;

    Priority(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
