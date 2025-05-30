package backend.academy.bot.controller.kafka;

import backend.academy.bot.schemas.requests.KafkaEventRequest;
import backend.academy.bot.schemas.responses.ListLinksResponse;
import backend.academy.bot.service.scrapper.kafka.KafkaResponseStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.message-transport", havingValue = "kafka")
@KafkaListener(topics = "${kafka.topic.list-links}")
public class ListConsumer {
    private final KafkaResponseStore kafkaResponseStore;

    @KafkaHandler
    public void handleListLinksResponse(
        @Payload ListLinksResponse response, @Header(KafkaHeaders.RECEIVED_KEY) Long chatId) {
        kafkaResponseStore.completeRequest(chatId, response);
    }

    @KafkaHandler
    public void handleKafkaEventRequest(
        @Payload KafkaEventRequest response, @Header(KafkaHeaders.RECEIVED_KEY) Long chatId) {
    }
}
