package backend.academy.bot.service;

import backend.academy.bot.repository.UserRepository;
import backend.academy.bot.schemas.models.UserStates;
import backend.academy.bot.service.commandhandlers.CommandHandler;
import backend.academy.bot.service.commandhandlers.HelpHandler;
import backend.academy.bot.service.commandhandlers.ListHandler;
import backend.academy.bot.service.commandhandlers.StartHandler;
import backend.academy.bot.service.commandhandlers.TrackHandler;
import backend.academy.bot.service.commandhandlers.UntrackHandler;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class MessageService {
    private final UserRepository userRepository;
    private final Map<String, CommandHandler> commandHandlers;

    MessageService(
            UserRepository userRepository,
            StartHandler startHandler,
            HelpHandler helpHandler,
            TrackHandler trackHandler,
            UntrackHandler untrackHandler,
            ListHandler listHandler) {
        this.userRepository = userRepository;

        this.commandHandlers = new HashMap<>();

        commandHandlers.put(BotCommands.START.command, startHandler);
        commandHandlers.put(BotCommands.HELP.command, helpHandler);
        commandHandlers.put(BotCommands.TRACK.command, trackHandler);
        commandHandlers.put(BotCommands.UNTRACK.command, untrackHandler);
        commandHandlers.put(BotCommands.LIST.command, listHandler);
    }

    public String processingRequest(long chatId, String text) {
        if (!userRepository.contain(chatId) && !text.equals("/start")) {
            return "Введите /start";
        } else if (text.startsWith("/")) {
            return processingCommand(chatId, text);
        } else {
            return processingMessage(chatId, text);
        }
    }

    private String processingCommand(long chatId, String text) {
        CommandHandler handler = commandHandlers.get(text);

        if (handler != null) {
            return handler.handle(chatId, text);
        } else {
            return "Такая команда отсутствует";
        }
    }

    private String processingMessage(long chatId, String text) {
        return switch (userRepository.getUser(chatId).state()) {
            case UserStates.FREE -> "Невозможно обработать сообщение";
            case UserStates.WAIT_UNTRACK_LINK -> commandHandlers
                    .get(BotCommands.UNTRACK.command)
                    .handle(chatId, text);
            default -> commandHandlers.get(BotCommands.TRACK.command).handle(chatId, text);
        };
    }
}
