package backend.academy.bot.service.commandhandlers;

public interface CommandHandler {
    String command();

    String handle(Long chatId, String message);
}
