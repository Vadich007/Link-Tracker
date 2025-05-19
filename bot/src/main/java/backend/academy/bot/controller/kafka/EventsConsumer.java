package backend.academy.bot.controller.kafka;

import backend.academy.bot.schemas.responses.LinkUpdateResponse;
import backend.academy.bot.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.message-transport", havingValue = "kafka")
@KafkaListener(topics = "${kafka.topic.events}")
public class EventsConsumer {
    private final NotificationService notificationService;

    @KafkaHandler
    public void eventsListen(@Payload LinkUpdateResponse linkUpdateResponse) {
        log.info("Link update received {} for users {}", linkUpdateResponse.url(), linkUpdateResponse.tgChatIds());
        String message = "Новое обновление\n" + linkUpdateResponse.url() + "\n" + linkUpdateResponse.description();

        for (Long chat : linkUpdateResponse.tgChatIds()) {
            notificationService.notifyUser(chat, message);
            log.info("Notification of link update {} sent for the user {}", linkUpdateResponse.url(), chat);
        }
    }
}
