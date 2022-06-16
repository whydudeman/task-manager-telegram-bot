package kz.leansolutions.telegram_task_manager_bot.telegram.controller.impl;

import kz.leansolutions.telegram_task_manager_bot.model.Status;
import kz.leansolutions.telegram_task_manager_bot.model.Task;
import kz.leansolutions.telegram_task_manager_bot.model.User;
import kz.leansolutions.telegram_task_manager_bot.service.TaskService;
import kz.leansolutions.telegram_task_manager_bot.service.UserService;
import kz.leansolutions.telegram_task_manager_bot.telegram.controller.TelegramReceiverController;
import kz.leansolutions.telegram_task_manager_bot.telegram.model.BotState;
import kz.leansolutions.telegram_task_manager_bot.telegram.model.BotUser;
import kz.leansolutions.telegram_task_manager_bot.telegram.model.ButtonCommand;
import kz.leansolutions.telegram_task_manager_bot.telegram.model.NotificationType;
import kz.leansolutions.telegram_task_manager_bot.telegram.service.BotUserService;
import kz.leansolutions.telegram_task_manager_bot.telegram.service.impl.TelegramNotificationService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.Optional;

@Component
@AllArgsConstructor
public class TelegramReceiverControllerImpl implements TelegramReceiverController {
    private final BotUserService botUserService;
    private final TelegramNotificationService telegramNotificationService;
    private final TaskService taskService;
    private final UserService userService;


    @Override
    public void handleStart(String chatId) {
        BotUser bot = botUserService.getByChatId(chatId);

        if (bot == null) {
            bot = new BotUser();
            bot.setChatId(chatId);

        }

        if (bot.getUser() != null) {
            telegramNotificationService.sendNotification(null, NotificationType.ALREADY_REGISTERED, chatId);
            return;
        }
        bot.setCurrentState(BotState.REGISTRATION);
        botUserService.save(bot);

        telegramNotificationService.sendNotification(null, NotificationType.REGISTRATION, chatId);
    }

    @Override
    public void handleRegistrationCommand(String chatId) {
        handleStart(chatId);
    }

    @Override
    public void handleRegistrationAnswer(BotUser bot, Message message) {
        String chatId = bot.getChatId();
        String updateText = message.getText();

        if (bot.getUser() != null) {
            bot.setCurrentState(null);
            botUserService.save(bot);
            telegramNotificationService.sendNotification(null, NotificationType.ALREADY_REGISTERED, chatId);
            return;
        }

        if (!ButtonCommand.YES.getDescription().equals(updateText)) {
            bot.setCurrentState(null);
            botUserService.save(bot);
            telegramNotificationService.sendNotification(null, NotificationType.RESTART, chatId);
            return;
        }

        bot.setCurrentState(BotState.PHONE_REQUEST);
        botUserService.save(bot);

        telegramNotificationService.sendNotification(null, NotificationType.PHONE, chatId);
    }

    @Override
    public void handlePhoneAnswer(BotUser bot, Message message) {
        String chatId = bot.getChatId();

        System.out.println(message.getContact());
        if (message.getContact() == null
                || message.getContact().getPhoneNumber() == null
                || message.getContact().getPhoneNumber().isBlank()) {
            bot.setCurrentState(null);
            botUserService.save(bot);
            telegramNotificationService.sendNotification(null, NotificationType.INVALID_PHONE, chatId);
            return;
        }
        String phone = message.getContact().getPhoneNumber();
        Optional<User> userOptional = userService.getUserByPhone(phone);
        if (userOptional.isEmpty()) {
            bot.setCurrentState(null);
            botUserService.save(bot);
            telegramNotificationService.sendNotification(null, NotificationType.NO_USER_WITH_THIS_PHONE, chatId);
            return;
        }

        BotUser botUserByUser = botUserService.getByUser(userOptional.get());
        if (botUserByUser != null) {
            botUserService.deleteById(chatId);
            bot = botUserByUser;
            bot.setChatId(chatId);
        }

        bot.setUser(userOptional.get());
        bot.setCurrentState(null);
        botUserService.save(bot);
        telegramNotificationService.sendNotification(null, NotificationType.REGISTRATION_SUCCESS, chatId);
    }

    @Override
    public void handleFinishTask(String chatId, String taskId) {
        BotUser bot = botUserService.getByChatId(chatId);
        System.out.println(taskId);
        if (taskId == null) {
            telegramNotificationService.sendNotification(null, NotificationType.INVALID_TASK_ID, chatId);
            return;
        }

        Task task = taskService.getById(Long.valueOf(taskId));
        if (task == null) {
            telegramNotificationService.sendNotification(null, NotificationType.INVALID_TASK_ID, chatId);
            return;
        }

        if (!task.getExecutor().equals(bot.getUser())) {
            telegramNotificationService.sendNotification(null, NotificationType.USER_IS_NOT_ALLOWED, chatId);
            return;
        }

        if (task.getStatus().equals(Status.DONE)) {
            telegramNotificationService.sendNotification(null, NotificationType.TASK_IS_ALREADY_FINISHED, chatId);
            return;
        }

        finishTask(chatId, task);
    }

    @Override
    public void handlePageChange(String chatId, String pageNumber) {
        BotUser bot = botUserService.getByChatId(chatId);
        if (bot.getLastPinnedMessageId() == null && pageNumber == null)
            return;
        Integer page = Integer.valueOf(pageNumber);
        Page<Task> allByExecutor = taskService.getAllByExecutor(bot.getUser(), page, 3);
        telegramNotificationService.sendAllTaskNotification(allByExecutor, NotificationType.TASKS_PIN_UPDATE, chatId, bot.getLastPinnedMessageId());
    }

    private void finishTask(String chatId, Task task) {
        task.setStatus(Status.DONE);
        taskService.save(task);
        telegramNotificationService.sendNotification(task, NotificationType.TASK_DONE, chatId);
    }
}
