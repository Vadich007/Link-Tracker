package backend.academy.bot.controller;

import backend.academy.bot.schemas.responses.LinkUpdate;
import backend.academy.bot.service.NotificationService;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/updates")
@Slf4j
public class BotController {
    private NotificationService notificationService;

    @PostMapping
    public void updateLink(@NotNull @RequestBody @Validated LinkUpdate linkUpdate) {
        log.info("Link update received {} for users {}", linkUpdate.url(), linkUpdate.tgChatIds());
        String message = "Новое обновление\n" + linkUpdate.url() + "\n" + linkUpdate.description();
        for (Long chat : linkUpdate.tgChatIds()) {
            notificationService.notifyUser(chat, message);
            log.info("Notification of link update {} sent for the user {}", linkUpdate.url(), chat);
        }
    }
}
