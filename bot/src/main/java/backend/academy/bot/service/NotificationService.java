package backend.academy.bot.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class NotificationService {
    private TelegramService telegramService;

    public void notifyUser(long chatId, String message) {
        telegramService.sendMessage(chatId, message);
    }
}
