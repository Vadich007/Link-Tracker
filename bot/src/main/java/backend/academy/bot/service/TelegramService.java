package backend.academy.bot.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SetMyCommands;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TelegramService {
    private final TelegramBot telegramBot;
    private final MessageService messageService;

    TelegramService(TelegramBot telegramBot, MessageService messageService) {
        this.telegramBot = telegramBot;
        this.messageService = messageService;

        SetMyCommands commands = new SetMyCommands(Arrays.stream(BotCommands.values())
                .map(BotCommands::toTgBotCommand)
                .toArray(BotCommand[]::new));
        telegramBot.execute(commands);

        listenMessage();
    }

    public void listenMessage() {
        telegramBot.setUpdatesListener(updates -> {
            for (Update update : updates) {
                long chatId = update.message().chat().id();
                String text = update.message().text();
                log.info("Received messages from a user with an id {}\n {}", chatId, text);
                String response = messageService.processingRequest(chatId, text);
                sendMessage(chatId, response);
            }
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    public void sendMessage(long chatId, String message) {
        telegramBot.execute(new SendMessage(chatId, message));
        log.info("A message was sent to the user with an id {}\n {}", chatId, message);
    }
}
