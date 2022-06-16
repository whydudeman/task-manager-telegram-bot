package kz.leansolutions.telegram_task_manager_bot.telegram.service.impl;

import kz.leansolutions.telegram_task_manager_bot.telegram.controller.TelegramReceiverController;
import kz.leansolutions.telegram_task_manager_bot.telegram.model.BotCommand;
import kz.leansolutions.telegram_task_manager_bot.telegram.model.BotState;
import kz.leansolutions.telegram_task_manager_bot.telegram.model.BotUser;
import kz.leansolutions.telegram_task_manager_bot.telegram.model.ButtonCommand;
import kz.leansolutions.telegram_task_manager_bot.telegram.service.BotUserService;
import kz.leansolutions.telegram_task_manager_bot.telegram.service.TelegramMessageReceiver;
import kz.leansolutions.telegram_task_manager_bot.utils.AppUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Service
@AllArgsConstructor
public class TelegramMessageReceiverImpl implements TelegramMessageReceiver {
    private final BotUserService botUserService;
    private final TelegramReceiverController telegramReceiverController;

    @Override
    public void handleUpdate(Update update) {
        Message message = update.getMessage();
        if (message != null) {
            String text = update.getMessage().getText();
            BotCommand command = BotCommand.getByValue(text);
            String chatId = String.valueOf(update.getMessage().getChatId());
            if (command != null) {
                switch (command) {
                    case START:
                        telegramReceiverController.handleStart(chatId);
                        break;
                    case REGISTRATION:
                        telegramReceiverController.handleRegistrationCommand(chatId);
                        break;
                    case FINISH_TASK:
                        telegramReceiverController.handleFinishTask(chatId, AppUtils.getLongNumberFromText(text));
                }
            } else {
                handleState(update);
            }
        } else if (update.hasCallbackQuery()) {
            handleCallbackQuery(update);
        }
    }

    private void handleState(Update update) {
        String chatId = String.valueOf(update.getMessage().getChatId());
        BotUser bot = botUserService.getByChatId(chatId);
        if (bot == null) {
            return;
        }

        if (BotState.REGISTRATION.equals(bot.getCurrentState()))
            telegramReceiverController.handleRegistrationAnswer(bot, update.getMessage());

        else if (BotState.PHONE_REQUEST.equals(bot.getCurrentState()))
            telegramReceiverController.handlePhoneAnswer(bot, update.getMessage());
    }

    private void handleCallbackQuery(Update update) {
        Message message = update.getCallbackQuery().getMessage();
        String[] callbackData = update.getCallbackQuery().getData().split("/");
        String commandValue = callbackData[0];
        ButtonCommand command = ButtonCommand.getByValue(commandValue);
        log.warn("handleCallbackQuery -> Command: {}, Value: {}, ButtonCommand: {}", commandValue, callbackData[1], command);
        String chatId = message.getChatId().toString();

        if (command == null) {
            return;
        }

        if (command.equals(ButtonCommand.PAGE)) {
            String pageNumber = callbackData[1];
            log.info("Starting process of changing page to: {} ..", pageNumber);
            telegramReceiverController.handlePageChange(chatId, pageNumber);
        }
    }
}

