package backend.academy.scrapper.controller.kafka;

import backend.academy.scrapper.repository.chat.ChatRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import java.util.NoSuchElementException;

@Component
@AllArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.message-transport", havingValue = "kafka")
public class ChatConsumer {
    private final ChatRepository repository;

    @KafkaListener(topics = "${kafka.topic.chats}")
    public void chatListen(@Header(KafkaHeaders.RECEIVED_KEY) Long id) {
        if (repository.containChat(id)) throw new NoSuchElementException("Чат с таким id уже зарегистрирован");
        repository.addChat(id);
        log.info("Authorization of a user with an id {}", id);
    }
}
