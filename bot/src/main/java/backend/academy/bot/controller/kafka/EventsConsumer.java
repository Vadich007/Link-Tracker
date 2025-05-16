package backend.academy.bot.controller.kafka;

import backend.academy.bot.schemas.responses.LinkUpdate;
import backend.academy.bot.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.message-transport", havingValue = "kafka")
public class EventsConsumer {
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @SneakyThrows
    @KafkaListener(topics = "${kafka.topic.events}")
    public void eventsListen(@Payload String value) {

        LinkUpdate linkUpdate = objectMapper.readValue(value, LinkUpdate.class);

        log.info("Link update received {} for users {}", linkUpdate.url(), linkUpdate.tgChatIds());
        String message = "Новое обновление\n" + linkUpdate.url() + "\n" + linkUpdate.description();

        for (Long chat : linkUpdate.tgChatIds()) {
            notificationService.notifyUser(chat, message);
            log.info("Notification of link update {} sent for the user {}", linkUpdate.url(), chat);
        }
    }
}
