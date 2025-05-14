package backend.academy.bot.service;

import com.pengrad.telegrambot.model.BotCommand;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum BotCommands {
    START("/start", "Регистрация пользователя."),
    HELP("/help", "Вывод списка доступных команд с описанием."),
    TRACK("/track", "Начать отслеживание ссылки."),
    UNTRACK("/untrack", "Прекратить отслеживание ссылки."),
    LIST("/list", "Показать список отслеживаемых ссылок.");

    public final String command;
    public final String description;

    public BotCommand toTgBotCommand() {
        return new BotCommand(this.command, this.description);
    }
}
