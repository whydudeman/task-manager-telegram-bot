package kz.leansolutions.telegram_task_manager_bot.model;

public enum Status {
    IN_PROGRESS("В работе"),
    DONE("Исполенено"),
    NOT_DONE("Не исполнено");

    private String name;

    Status(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
