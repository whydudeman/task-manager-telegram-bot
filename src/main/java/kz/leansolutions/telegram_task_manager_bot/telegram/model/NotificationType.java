package kz.leansolutions.telegram_task_manager_bot.telegram.model;

import java.util.Arrays;
import java.util.List;

public enum NotificationType {
    TASK_UPDATED(null),
    TASK_CREATED(null),
    TASK_DEADLINE_PASSED(null),
    TASK_DONE(null),
    TASKS_PIN(Arrays.asList(AdditionalType.values())),
    TASKS_PIN_UPDATE(Arrays.asList(AdditionalType.values())),
    REGISTRATION(null),
    ALREADY_REGISTERED(null),
    RESTART(null),
    PHONE(null),
    INVALID_PHONE(null),
    REGISTRATION_SUCCESS(null),
    INVALID_TASK_ID(null),
    USER_IS_NOT_ALLOWED(null),
    TASK_IS_ALREADY_FINISHED(null), NO_USER_WITH_THIS_PHONE(null);

    private List<AdditionalType> additionalTypes;

    NotificationType(List<AdditionalType> additionalTypes) {
        this.additionalTypes = additionalTypes;
    }

    public List<AdditionalType> getAdditionalTypes() {
        return additionalTypes;
    }

    public enum AdditionalType {
        TASKS_PIN_MAIN_HEADER(false, true),
        TASKS_PIN_MAIN_TEXT(true, true),
        TASKS_PIN_ADDITION_HEADER(false, false),
        TASKS_PIN_ADDITIONAL_TEXT(true, false);

        private boolean repeated;
        private boolean main;

        AdditionalType(boolean repeated, boolean main) {
            this.repeated = repeated;
            this.main = main;
        }

        public boolean isRepeated() {
            return repeated;
        }

        public boolean isMain() {
            return main;
        }
    }
}
