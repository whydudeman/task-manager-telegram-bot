package kz.leansolutions.telegram_task_manager_bot.telegram.service.impl;

import kz.leansolutions.telegram_task_manager_bot.model.Task;
import kz.leansolutions.telegram_task_manager_bot.telegram.model.BotLanguage;
import kz.leansolutions.telegram_task_manager_bot.telegram.model.BotUser;
import kz.leansolutions.telegram_task_manager_bot.telegram.model.ButtonCommand;
import kz.leansolutions.telegram_task_manager_bot.telegram.model.NotificationType;
import kz.leansolutions.telegram_task_manager_bot.telegram.service.BotUserService;
import kz.leansolutions.telegram_task_manager_bot.telegram.service.NotificationService;
import kz.leansolutions.telegram_task_manager_bot.telegram.service.TelegramBotService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.pinnedmessages.PinChatMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TelegramNotificationService extends NotificationService {
    @Lazy
    @Autowired
    private TelegramBotService telegramBotService;
    @Autowired
    private BotUserService botUserService;

    public TelegramNotificationService() {
        super(new HashMap<>() {{
            put(BotLanguage.RU, "ru-telegram-messages.json");
        }});
    }

    @Override
    public void sendNotification(Task task, NotificationType notificationType, String chatId) {
        if (notificationType == null || chatId == null) {
            return;
        }

        BotLanguage language = BotLanguage.RU; //TODO logic for choosing Language for future
        Map<String, String> values = getValues(task, notificationType);
        String message = getMessage(notificationType, values, language);

        if (message.isEmpty()) {
            log.error("CANNOT_GET_NOTIFICATION_MESSAGE TASK_ID: {}, NOTIFICATION_TYPE: {}",
                    task != null ? task.getId() : null, notificationType.toString());
            throw new RuntimeException("CANNOT_GET_NOTIFICATION_MESSAGE");
        }

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(message);

        ReplyKeyboardMarkup replyKeyboard = getReplyKeyboard(notificationType, language);

        sendMessage
                .setReplyMarkup(
                        Objects.requireNonNullElseGet(replyKeyboard, () -> new ReplyKeyboardRemove(true)));

        try {
            telegramBotService.execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendAllTaskNotification(Page<Task> tasks, NotificationType notificationType, String chatId) {
        if (notificationType == null || chatId == null) {
            return;
        }
        BotLanguage language = BotLanguage.RU; //TODO logic for choosing Language for future
        String message = generateAllTaskMessage(tasks, notificationType, chatId, language);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(message);

        InlineKeyboardMarkup keyboardMarkup = getPageInlineKeyboard(tasks.getNumber(), tasks.getTotalPages());
        if (keyboardMarkup != null) {
            sendMessage.setReplyMarkup(keyboardMarkup);
        }

        try {
            Message execute = telegramBotService.execute(sendMessage);
            PinChatMessage pinChatMessage = PinChatMessage.builder().chatId(chatId).messageId(execute.getMessageId()).build();
            telegramBotService.execute(pinChatMessage);
            BotUser botUser = botUserService.getByChatId(chatId);
            botUser.setLastPinnedMessageId(execute.getMessageId());
            botUserService.save(botUser);

        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendAllTaskNotification(Page<Task> tasks, NotificationType notificationType, String chatId, Integer lastPinnedMessageId) {
        if (notificationType == null || chatId == null) {
            return;
        }
        BotLanguage language = BotLanguage.RU; //TODO logic for choosing Language for future
        String message = generateAllTaskMessage(tasks, notificationType, chatId, language);

        EditMessageText editMessage = new EditMessageText();
        editMessage.setChatId(chatId);
        editMessage.setMessageId(lastPinnedMessageId);
        editMessage.setText(message);

        InlineKeyboardMarkup keyboardMarkup = getPageInlineKeyboard(tasks.getNumber(), tasks.getTotalPages());
        if (keyboardMarkup != null) {
            editMessage.setReplyMarkup(keyboardMarkup);
        }

        try {
            telegramBotService.execute(editMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private String generateAllTaskMessage(Page<Task> tasks, NotificationType notificationType, String chatId, BotLanguage language) {
        StringBuilder messageBuilder = new StringBuilder();

        Predicate<Task> todayTaskPredicate = t -> t.getDeadline().toLocalDate().equals(LocalDate.now());
        log.info("Tasks Deadline: {}, Current Date: {}", null, LocalDate.now());
        Predicate<Task> additionalPredicate = t -> !t.getDeadline().toLocalDate().equals(LocalDate.now());

        List<Task> mainTasks = tasks.stream().filter(todayTaskPredicate).collect(Collectors.toList());
        List<Task> additionalTasks = tasks.stream().filter(additionalPredicate).collect(Collectors.toList());

        if (notificationType.getAdditionalTypes() != null)
            notificationType.getAdditionalTypes().forEach(
                    additionalType -> {
                        if (!additionalType.isRepeated()) {
                            messageBuilder.append(getMessage(additionalType, new HashMap<>(), language));
                        } else if (additionalType.isMain()) {
                            mainTasks.forEach(t -> {
                                Map<String, String> values = getValues(t, notificationType);
                                messageBuilder.append(getMessage(additionalType, values, language));
                            });
                        } else additionalTasks.forEach(t -> {
                            Map<String, String> values = getValues(t, notificationType);
                            messageBuilder.append(getMessage(additionalType, values, language));
                        });
                    }
            );

        String message = messageBuilder.toString();
        log.info("Generated Message: {}, Tasks size: {}, Main Tasks size: {}, Additional Tasks Size: {}",
                message, tasks.getTotalElements(), mainTasks.size(), additionalTasks.size());

        if (message.isEmpty()) {
            log.error("CANNOT_SEND_ALL_TASKS_NOTIFICATION_MESSAGE chatId: {}, NOTIFICATION_TYPE: {}",
                    chatId, notificationType.toString());
            throw new RuntimeException("CANNOT_GET_NOTIFICATION_MESSAGE");
        }
        return message;
    }

    private Map<String, String> getValues(Task task, NotificationType notificationType) {
        Map<String, String> values = new HashMap<>();

        DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_DATE;
        switch (notificationType) {
            case TASK_CREATED:
            case TASK_UPDATED:
            case TASK_DEADLINE_PASSED:
            case TASKS_PIN:
            case TASK_DONE:
            case TASKS_PIN_UPDATE:
                values.put("id", String.valueOf(task.getId()));
                values.put("name", task.getName());
                values.put("projectName", task.getProjectName());
                values.put("description", task.getDescription());
                values.put("status", task.getStatus().getName());
                values.put("manager", task.getManager().getName());
                values.put("deadline", dateFormatter.format(task.getDeadline()));
                values.put("daysToDeadline", String.valueOf(ChronoUnit.DAYS.between(task.getDeadline(), LocalDateTime.now())));
                values.put("priority", task.getPriority().getName());
                values.put("executor", task.getExecutor().getName());
                values.put("difficulty", String.valueOf(task.getDifficulty()));
                break;
            default:
        }

        return values;
    }

    private ReplyKeyboardMarkup getReplyKeyboard(NotificationType notificationType, BotLanguage language) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        replyKeyboardMarkup.setResizeKeyboard(true);

        List<KeyboardRow> keyboard = new ArrayList<>();

        switch (notificationType) {
            case REGISTRATION:
                keyboard.add(getKeyboardRow(ButtonCommand.YES.getDescription(), ButtonCommand.NO.getDescription()));
                break;
            case PHONE:
                keyboard.add(
                        new KeyboardRow(
                                Collections.singletonList(
                                        getRequestContactButton(
                                                getMessage(notificationType, new HashMap<>(), language)))));
                break;
            default:
                return null;
        }

        replyKeyboardMarkup.setKeyboard(keyboard);
        return replyKeyboardMarkup;
    }

    private KeyboardRow getKeyboardRow(String... buttonTexts) {
        List<KeyboardButton> buttons = new ArrayList<>();

        for (String buttonText : buttonTexts) {
            buttons.add(getButton(buttonText));
        }

        return new KeyboardRow(buttons);
    }

    private KeyboardButton getButton(String text) {
        KeyboardButton button = new KeyboardButton();
        button.setText(text);
        return button;
    }

    private KeyboardButton getRequestContactButton(String text) {
        return KeyboardButton
                .builder()
                .text(text)
                .requestContact(true)
                .build();
    }

    private InlineKeyboardMarkup getPageInlineKeyboard(Integer currentPage, Integer totalPages) {
        if (totalPages > 1) {
            List<List<InlineKeyboardButton>> keyBoardMatrix = new ArrayList<>();
            List<InlineKeyboardButton> keyboardButtons = new ArrayList<>();
            for (int i = 0; i < totalPages; i++) {

                String buttonText = String.valueOf(i + 1);
                if (i == currentPage)
                    buttonText = "-" + buttonText + "-";
                InlineKeyboardButton button = InlineKeyboardButton.builder().text(buttonText).callbackData(ButtonCommand.PAGE.getValue() + "/" + i).build();
                keyboardButtons.add(button);
            }
            keyBoardMatrix.add(keyboardButtons);

            InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
            keyboardMarkup.setKeyboard(keyBoardMatrix);
            return keyboardMarkup;
        }
        return null;
    }

}
