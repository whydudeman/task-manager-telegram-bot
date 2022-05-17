package kz.leansolutions.telegram_task_manager_bot.model;

public enum BotCommands {
    START("/start"),
    FINISH_TASK("/finish/"),
    CHANGE_PINNED_MSG_PAGE("/change_page/"),
    GET_TASKS("/tasks");

    private String command;

    BotCommands(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }
}
