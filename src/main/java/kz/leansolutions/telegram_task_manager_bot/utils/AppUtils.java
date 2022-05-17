package kz.leansolutions.telegram_task_manager_bot.utils;

import javax.validation.constraints.NotNull;

public final class AppUtils {
    public static String getLongNumberFromText(@NotNull String text) {
        String onlyNumbersText = text.replaceAll("[^0-9]", "");
        if (onlyNumbersText.isBlank())
            return null;
        return onlyNumbersText;
    }
}
