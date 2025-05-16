package backend.academy.bot.controller.kafka;

import backend.academy.bot.schemas.responses.ListLinksResponse;
import backend.academy.bot.service.scrapper.kafka.KafkaResponseStore;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.message-transport", havingValue = "kafka")
public class ListConsumer {
    private final KafkaResponseStore kafkaResponseStore;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @SneakyThrows
    @KafkaListener(topics = "${kafka.topic.list-links}")
    public void handleListLinksResponse(@Payload String response,
                                        @Header(KafkaHeaders.RECEIVED_KEY) Long chatId) {
        if (response.equals("GET")) return;

        kafkaResponseStore.completeRequest(chatId, objectMapper.readValue(response, ListLinksResponse.class));
    }
}
