package kz.leansolutions.telegram_task_manager_bot.bot;

import kz.leansolutions.telegram_task_manager_bot.config.BotConfig;
import kz.leansolutions.telegram_task_manager_bot.model.*;
import kz.leansolutions.telegram_task_manager_bot.service.BotUserService;
import kz.leansolutions.telegram_task_manager_bot.service.TaskService;
import kz.leansolutions.telegram_task_manager_bot.service.UserService;
import kz.leansolutions.telegram_task_manager_bot.utils.AppUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.pinnedmessages.PinChatMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class TaskManagerBot extends TelegramLongPollingBot {
    private static final String ACCESS_DENIED_MESSAGE = "У вас нет доступа!!!";
    private static final String DEFAULT_ERROR_MESSAGE = "Неизвестная команда!!!";
    private static final String ERROR_MESSAGE_TEXT = "Ошибка в команде: *%s*, Подсказка: ```%s```";
    private static final String ENTER_PHONE = "Пожалуйста введите свой номер для регистрации";
    private static final String SEND_PHONE = "Отправить мой номер телефона для регистрации";
    private static final String AUTH_SUCCESS = "Вы успешно зарегистрированы";
    private static final int TASKS_SIZE = 3;
    @Autowired
    private BotUserService botUserService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private UserService userService;

    @Override
    public String getBotToken() {
        return BotConfig.CHANNEL_TOKEN;
    }

    @Override
    public String getBotUsername() {
        return BotConfig.CHANNEL_USER;
    }

    @Override
    public void onUpdateReceived(Update update) {

        try {
            Message message = update.getMessage();

            if (message == null) {
                handleCallBackQuery(update);
            } else {
                handleUpdateMessage(message);
            }

        } catch (Exception e) {
            log.error(e.getLocalizedMessage(), e);
        }
    }

    private void handleCallBackQuery(Update update) {
        String chatId = update.getCallbackQuery().getMessage().getChatId().toString();
        String callBackCommand = update.getCallbackQuery().getData();
        BotUser botUser = botUserService.getByIdOrNew(chatId);
        if (botUser.isRegistered() && botUser.getLastPinnedMessageId() != null) {
            if (callBackCommand.startsWith("page")) {
                String pageAsString = AppUtils.getLongNumberFromText(callBackCommand);
                if (pageAsString == null) {
                    defaultErrorMessage(chatId);
                    log.error("Invalid CallBack Command {}", callBackCommand);
                    return;
                }
                Integer page = Integer.valueOf(pageAsString);
                Page<Task> allByExecutor = taskService.getAllByExecutor(botUser.getUser(), page, TASKS_SIZE);
                String tasksMsg = getPeriodTasksMsgFromList(allByExecutor.toList());
                editPageableTasksMessage(chatId, botUser.getLastPinnedMessageId(), tasksMsg, allByExecutor);
            }
        } else {
            defaultAccessDeniedMessage(chatId);
        }
        botUserService.save(botUser);
    }

    private void handleUpdateMessage(Message message) {
        String chatId = message.getChatId().toString();
        BotUser botUser = botUserService.getByIdOrNew(chatId);

        if (botUser.isInRegistration()) {
            authorize(botUser, message);
        }

        if (message.hasText()) {
            String messageText = message.getText();
            if (messageText.equalsIgnoreCase(BotCommands.START.getCommand())) {
                authenticate(botUser, message);
            } else if (messageText.startsWith(BotCommands.FINISH_TASK.getCommand())) {
                processFinishTaskCommand(botUser, messageText);
            } else if (messageText.startsWith(BotCommands.GET_TASKS.getCommand()))
                sendMessage(chatId, "Данная функция на разработке!!!");
            else defaultErrorMessage(chatId);
        }

        botUserService.save(botUser);
    }

    private void authorize(BotUser botUser, Message message) {
        String chatId = String.valueOf(message.getChatId());
        if (message.getContact() == null
                || message.getContact().getPhoneNumber() == null
                || message.getContact().getPhoneNumber().isBlank()) {
            botUser.setInRegistration(false);
            botUserService.save(botUser);
            defaultAccessDeniedMessage(chatId);
            return;
        }
        String phone = message.getContact().getPhoneNumber();
        log.info(phone);
        Optional<User> userOptional = userService.getUserByPhone(phone);
        if (userOptional.isEmpty()) {
            defaultAccessDeniedMessage(chatId);
            return;
        }

        BotUser botUserByUser = botUserService.getByUser(userOptional.get());
        if (botUserByUser != null) {
            botUserService.deleteById(chatId);
            botUser = botUserByUser;
            botUser.setChatId(chatId);
        }

        botUser.setInRegistration(false);
        botUser.setRegistered(true);
        botUser.setUser(userOptional.get());
        botUserService.save(botUser);
        sendMessage(chatId, AUTH_SUCCESS);
    }

    private void processFinishTaskCommand(BotUser botUser, String messageText) {
        String chatId = botUser.getChatId();
        String taskId = AppUtils.getLongNumberFromText(messageText);

        if (taskId == null) {
            sendErrorMessageWithLog(chatId, messageText, " Вы не указали номер задачи!!!");
            return;
        }

        Task task = taskService.getById(taskId);
        if (task == null) {
            sendErrorMessageWithLog(chatId, messageText, " Такой задачи не существует!!!");
            return;
        }

        if (!task.getExecutor().equals(botUser.getUser())) {
            sendErrorMessageWithLog(chatId, messageText, " У вас нет прав на эту задачу!!!");
            return;
        }

        if (task.getStatus().equals(Status.DONE)) {
            sendErrorMessageWithLog(chatId, messageText, " Задача уже выполнена!!!");
        }

        finishTask(botUser, task);
    }

    private void finishTask(BotUser botUser, Task task) {
        task.setStatus(Status.DONE);
        taskService.save(task);
        String taskTextFormat = "Поздравляю %s! Так держать!\nЗадача #%s %s выполнена!";

        sendMessage(botUser.getChatId(),
                String.format(
                        taskTextFormat,
                        botUser.getUser().getName(),
                        task.getId(),
                        task.getName()
                ));

        triggerToUpdateLastPinnedMessage(botUser);
    }

    public void generatePageableTaskMsg(User executor) {
        if (executor.getBotUser() == null)
            return;
        Page<Task> tasks = taskService.getAllByExecutor(executor, 0, TASKS_SIZE);
        String readyMessage = getPeriodTasksMsgFromList(tasks.toList());
        sendPaginatedTasksAsMsg(executor.getBotUser(), readyMessage, tasks);
    }

    private void sendPaginatedTasksAsMsg(BotUser botUser, String text, Page<Task> tasks) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(botUser.getChatId()));
        message.setText(text);

        if (tasks.getTotalPages() > 1) {
            List<List<InlineKeyboardButton>> keyBoardMatrix = new ArrayList<>();
            List<InlineKeyboardButton> keyboardButtons = new ArrayList<>();
            for (int i = 0; i < tasks.getTotalPages(); i++) {

                String buttonText = String.valueOf(i + 1);
                if (i == tasks.getNumber())
                    buttonText = String.join("-", buttonText, "-");
                InlineKeyboardButton button = InlineKeyboardButton.builder().text(buttonText).callbackData("page/" + i).build();
                keyboardButtons.add(button);
            }
            keyBoardMatrix.add(keyboardButtons);

            InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
            keyboardMarkup.setKeyboard(keyBoardMatrix);
            message.setReplyMarkup(keyboardMarkup);
        }
        Message executedMessage = executeMessage(message);
        pinMessage(executedMessage);

        botUser.setLastPinnedMessageId(executedMessage.getMessageId());
        botUserService.save(botUser);
    }

    private void editPageableTasksMessage(String chatId, Integer messageId, String updateText, Page<Task> tasks) {
        EditMessageText message = new EditMessageText();
        message.setChatId(chatId);
        message.setMessageId(messageId);
        message.setText(updateText);

        if (tasks.getTotalPages() > 1) {
            List<List<InlineKeyboardButton>> keyBoardMatrix = new ArrayList<>();
            List<InlineKeyboardButton> keyboardButtons = new ArrayList<>();
            for (int i = 0; i < tasks.getTotalPages(); i++) {

                String buttonText = String.valueOf(i + 1);
                if (i == tasks.getNumber())
                    buttonText = String.join("-", buttonText, "-");
                InlineKeyboardButton button = InlineKeyboardButton.builder().text(buttonText).callbackData("page/" + i).build();
                keyboardButtons.add(button);
            }
            keyBoardMatrix.add(keyboardButtons);

            InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
            keyboardMarkup.setKeyboard(keyBoardMatrix);
            message.setReplyMarkup(keyboardMarkup);
        }
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error(e.getLocalizedMessage(), e);
        }
    }

    private String getPeriodTasksMsgFromList(List<Task> tasks) {
        String taskTextFormat = "\uD83D\uDD25#%s. %s\n Проект: %s\n Статус: %s\n " +
                "Постановщик: %s\n Срок: %s(осталось %d дн)\n Описание: %s\n" +
                "Оценка сложности: %d\nПриоритет: %s\n" +
                "Выполнить задачу: /finishTask/%s\nПодробная информация  https://taskmanager.com/task/%s \n";

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\uD83D\uDD25 Задачи на сегодня:\n\n");
        if (tasks.isEmpty())
            stringBuilder.append("\uD83D\uDE0A На сегодня у вас нет задач)");

        DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_DATE;

        for (Task task : tasks) {
            stringBuilder.append(
                    String.format(taskTextFormat,
                            task.getId(),
                            task.getName(),
                            task.getProjectName(),
                            task.getStatus().getName(),
                            task.getManager(),
                            dateFormatter.format(task.getDeadline()),
                            ChronoUnit.DAYS.between(task.getDeadline(), LocalDateTime.now()),
                            task.getDescription(),
                            task.getDifficulty(),
                            task.getPriority().getName(),
                            task.getId(),
                            task.getId()
                    )
            ).append("\n");
        }

        return stringBuilder.toString();
    }

    public void sendNewOneTask(Task task, User user) {
        if (user.getBotUser() == null)
            return;
        BotUser botUser = user.getBotUser();
        String msgHeader = "\uD83D\uDD25 %s, у Вас новая задача:\n";
        String taskTextFormat = msgHeader +
                "\uD83D\uDD25#%s. %s\n Проект: %s\n Статус: %s\n " +
                "Постановщик: %s\n Срок: %s (осталось %d дн)\n Описание: %s\n " +
                "Оценка сложности: %d\nПриоритет: %s\n" +
                "Выполнить задачу: /finishTask/%s\nПодробная информация  https://taskmanager.com/task/%s \n";
        DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_DATE;
        String stringBuilder =
                String.format(taskTextFormat,
                        botUser.getUser().getName(),
                        task.getId(),
                        task.getName(),
                        task.getProjectName(),
                        task.getStatus().getName(),
                        task.getManager(),
                        dateFormatter.format(task.getDeadline()),
                        ChronoUnit.DAYS.between(task.getDeadline(), LocalDateTime.now()),
                        task.getDescription(),
                        task.getDifficulty(),
                        task.getPriority().getName(),
                        task.getId(),
                        task.getId());
        sendMessage(botUser.getChatId(), stringBuilder);
        triggerToUpdateLastPinnedMessage(botUser);
    }

    public void sendUpdatedTask(Task task) {
        if (task.getExecutor() == null || task.getExecutor().getBotUser() == null)
            return;

        BotUser botUser = task.getExecutor().getBotUser();
        String msgHeader = "%s, В задачу #%s внесены изменения\n";
        String taskTextFormat = msgHeader +
                "\uD83D\uDD25 %s\n Проект: %s\n Статус: %s\n " +
                "Постановщик: %s\n Срок: %s (осталось %d дн)\n Описание: %s\n " +
                "Оценка сложности: %d\nПриоритет: %s\n" +
                "Выполнить задачу: /finishTask/%s\nПодробная информация  https://taskmanager.com/task/%s \n";
        DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_DATE;
        String stringBuilder =
                String.format(taskTextFormat,
                        botUser.getUser().getName(),
                        task.getId(),
                        task.getName(),
                        task.getProjectName(),
                        task.getStatus().getName(),
                        task.getManager(),
                        dateFormatter.format(task.getDeadline()),
                        ChronoUnit.DAYS.between(task.getDeadline(), LocalDateTime.now()),
                        task.getDescription(),
                        task.getDifficulty(),
                        task.getPriority().getName(),
                        task.getId(),
                        task.getId());
        sendMessage(botUser.getChatId(), stringBuilder);
        triggerToUpdateLastPinnedMessage(botUser);
    }

    public void sendPassedDeadlineTask(Task task) {
        if (task.getExecutor() == null || task.getExecutor().getBotUser() == null)
            return;
        BotUser botUser = task.getExecutor().getBotUser();
        String taskTextFormat = "%s, у Вас просроченная задача #%s\n" +
                "Проект: %s\n Статус: %s\n " +
                "Постановщик: %s\n Срок: %s (осталось %d дн)\n Описание: %s\n " +
                "Оценка сложности: %d\nПриоритет: %s\n" +
                "Выполнить задачу: /finishTask/%s\nПодробная информация  https://taskmanager.com/task/%s \n";
        DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_DATE;
        String stringBuilder = String.format(taskTextFormat,
                task.getExecutor().getName(),
                task.getId(),
                task.getProjectName(),
                task.getStatus().getName(),
                task.getManager(),
                dateFormatter.format(task.getDeadline()),
                ChronoUnit.DAYS.between(task.getDeadline(), LocalDateTime.now()),
                task.getDescription(),
                task.getDifficulty(),
                task.getPriority().getName(),
                task.getId(),
                task.getId());
        sendMessage(botUser.getChatId(), stringBuilder);
    }

    private void triggerToUpdateLastPinnedMessage(BotUser botUser) {
        if (botUser.getLastPinnedMessageId() == null) {
            return;
        }
        String updatedTasksMsg = getPeriodTasksMsgFromList(taskService.getAllByExecutor(botUser.getUser()));
        editPageableTasksMessage(
                botUser.getChatId(),
                botUser.getLastPinnedMessageId(), updatedTasksMsg,
                taskService.getAllByExecutor(botUser.getUser(), 0, TASKS_SIZE));
    }

    private void sendErrorMessageWithLog(String chatId, String messageText, String errorText) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(chatId);
        sendMessage.setText(String.format(ERROR_MESSAGE_TEXT, messageText.trim(), errorText.replace("\"", "\\\"")));
        sendMessage.enableMarkdown(true);

        executeMessage(sendMessage);
    }

    private void authenticate(BotUser botUser, Message message) {
        String chatId = String.valueOf(message.getChatId());
        if (botUser.isRegistered()) {
            botUser.setInRegistration(false);
            sendMessage(chatId, "Вы уже зарегестривованы!!!");
        } else {
            botUser.setChatId(chatId);
            botUser.setInRegistration(true);
            requestPhone(chatId);
        }
        botUserService.save(botUser);
    }

    private void requestPhone(String chatId) {
        SendMessage sendMessage = SendMessage
                .builder()
                .chatId(chatId)
                .text(SEND_PHONE)
                .build();

        ReplyKeyboardMarkup replyKeyboardMarkup = ReplyKeyboardMarkup.builder()
                .selective(true)
                .resizeKeyboard(true)
                .oneTimeKeyboard(true)
                .build();
        sendMessage.setReplyMarkup(replyKeyboardMarkup);


        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow keyboardFirstRow = new KeyboardRow();
        KeyboardButton keyboardButton = KeyboardButton
                .builder()
                .text(ENTER_PHONE)
                .requestContact(true)
                .build();
        keyboardFirstRow.add(keyboardButton);

        keyboard.add(keyboardFirstRow);

        replyKeyboardMarkup.setKeyboard(keyboard);
        executeMessage(sendMessage);
    }


    private void sendMessage(String chatId, String errorText) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(errorText);

        executeMessage(sendMessage);
    }

    private void pinMessage(Message message) {
        PinChatMessage sendMessage = new PinChatMessage();
        sendMessage.setChatId(Long.toString(message.getChatId()));
        sendMessage.setMessageId(message.getMessageId());
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error(e.getLocalizedMessage(), e);
        }
    }

    private void defaultErrorMessage(String chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(DEFAULT_ERROR_MESSAGE);
        executeMessage(sendMessage);
    }

    private void defaultAccessDeniedMessage(String chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(ACCESS_DENIED_MESSAGE);
        executeMessage(sendMessage);
    }


    private Message executeMessage(SendMessage sendMessage) {
        try {
            return execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error(e.getLocalizedMessage(), e);
        }
        return new Message();
    }
}
