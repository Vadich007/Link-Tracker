package backend.academy.bot.service.commandhandlers;

import backend.academy.bot.service.BotCommands;
import org.springframework.stereotype.Component;

@Component
public class HelpHandler implements CommandHandler {
    @Override
    public String command() {
        return BotCommands.HELP.name();
    }

    @Override
    public String handle(Long chatId, String message) {
        StringBuilder messageBuilder = new StringBuilder();
        for (BotCommands commands : BotCommands.values())
            messageBuilder
                    .append(commands.command)
                    .append(" - ")
                    .append(commands.description)
                    .append("\n");
        return messageBuilder.toString();
    }
}
