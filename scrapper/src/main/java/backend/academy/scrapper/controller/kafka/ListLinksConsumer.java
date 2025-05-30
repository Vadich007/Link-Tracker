package backend.academy.scrapper.controller.kafka;

import backend.academy.scrapper.repository.chat.ChatRepository;
import backend.academy.scrapper.repository.link.LinkRepository;
import backend.academy.scrapper.schemas.models.Link;
import backend.academy.scrapper.schemas.requests.KafkaEventRequest;
import backend.academy.scrapper.schemas.responses.bot.ListLinksResponse;
import backend.academy.scrapper.service.bot.KafkaBotService;
import java.util.List;
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
public class ListLinksConsumer {
    private final ChatRepository chatRepository;
    private final LinkRepository linkRepository;
    private final KafkaBotService kafkaBotService;

    @KafkaHandler
    public void getListLinksListen(@Payload KafkaEventRequest request, @Header(KafkaHeaders.RECEIVED_KEY) Long id) {
        if (!request.action().equals("GET_LINKS")) return;

        if (!chatRepository.containChat(id)) {
            log.error("An unsuccessful attempt to get a list of links from a non-existent user with id {}", id);
            return;
        }

        log.info("User with id {} requested a list of links", id);

        List<Link> links = linkRepository.getLinks(id);
        kafkaBotService.getLinks(id, new ListLinksResponse(links, links.size()));
    }

    @KafkaHandler
    public void listLinksListen(@Payload ListLinksResponse response, @Header(KafkaHeaders.RECEIVED_KEY) Long id) {
    }
}
