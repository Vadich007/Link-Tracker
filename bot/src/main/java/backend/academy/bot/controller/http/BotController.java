package backend.academy.bot.controller.http;

import backend.academy.bot.schemas.responses.LinkUpdateResponse;
import backend.academy.bot.service.NotificationService;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/updates")
@Slf4j
@ConditionalOnProperty(name = "app.message-transport", havingValue = "http")
public class BotController {
    private NotificationService notificationService;

    @PostMapping
    public void updateLink(@NotNull @RequestBody @Validated LinkUpdateResponse linkUpdateResponse) {
        log.info("Link update received {} for users {}", linkUpdateResponse.url(), linkUpdateResponse.tgChatIds());
        String message = "Новое обновление\n" + linkUpdateResponse.url() + "\n" + linkUpdateResponse.description();
        for (Long chat : linkUpdateResponse.tgChatIds()) {
            notificationService.notifyUser(chat, message);
            log.info("Notification of link update {} sent for the user {}", linkUpdateResponse.url(), chat);
        }
    }
}
