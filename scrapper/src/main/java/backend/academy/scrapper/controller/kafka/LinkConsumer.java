package backend.academy.scrapper.controller.kafka;

import backend.academy.scrapper.repository.chat.ChatRepository;
import backend.academy.scrapper.repository.link.LinkRepository;
import backend.academy.scrapper.schemas.models.Link;
import backend.academy.scrapper.schemas.requests.AddLinkRequest;
import backend.academy.scrapper.schemas.requests.RemoveLinkRequest;
import backend.academy.scrapper.schemas.responses.bot.ListLinksResponse;
import backend.academy.scrapper.service.bot.KafkaBotService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
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
public class LinkConsumer {
    private final ChatRepository chatRepository;
    private final LinkRepository linkRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final KafkaBotService kafkaBotService;

    @SneakyThrows
    @KafkaListener(topics = "${kafka.topic.add-link}")
    public void addLinkListen(@Payload String values,
                              @Header(KafkaHeaders.RECEIVED_KEY) Long id) {

        AddLinkRequest addLinkRequest = objectMapper.readValue(values, AddLinkRequest.class);

        if (!chatRepository.containChat(id)) {
            log.error(
                "Unsuccessful attempt to add a link {} from a non-existent user with an id {}",
                addLinkRequest.link(),
                id);
            throw new NoSuchElementException("Отсутствует чат с таким id");
        }

        if (chatRepository.getLinks(id).stream().anyMatch(l -> l.url().equals(addLinkRequest.link()))) {
            log.error("Unsuccessful attempt to add a link {} user with an id {}", addLinkRequest.link(), id);
            throw new NoSuchElementException("Ссылка уже отслеживается");
        }

        if (!linkRepository.containLink(addLinkRequest.link())) {
            linkRepository.addLink(addLinkRequest.link());
            log.info("User with id {} add link {} in database", id, addLinkRequest.link());
        }

        Link link = new Link(linkRepository.getLinkId(addLinkRequest.link()), addLinkRequest.link(), addLinkRequest.tags(), addLinkRequest.filters());
        chatRepository.subscribeLink(id, link);
        log.info("User with id {} subscribed to link {}", id, addLinkRequest.link());
    }

    @KafkaListener(topics = "${kafka.topic.remove-link}")
    @SneakyThrows
    public void removeLinkListen(@Payload String values,
                                 @Header(KafkaHeaders.RECEIVED_KEY) Long id) {

        RemoveLinkRequest removeLinkRequest = objectMapper.readValue(values, RemoveLinkRequest.class);

        if (!chatRepository.containChat(id)) {
            log.error(
                "Unsuccessful attempt to delete a link {} for a non-existent user with an id {}",
                removeLinkRequest.link(),
                id);
            throw new NoSuchElementException("Отсутствует чат с таким id");
        }

        Optional<Link> optional = chatRepository.getLinks(id).stream()
            .filter(l -> l.url().equals(removeLinkRequest.link()))
            .findFirst();
        if (optional.isEmpty()) {
            log.error(
                "User with id {} try to delete link {}, which does not exist in repository",
                id,
                removeLinkRequest.link());
            throw new IllegalArgumentException("Ссылка не найдена");
        }

        linkRepository.deleteChat(removeLinkRequest.link(), id);
        log.info("User with id {} unsubscribed from link {}", id, removeLinkRequest.link());

        if (linkRepository.getChats(removeLinkRequest.link()).isEmpty()) {
            log.info("Delete link {} from repository", removeLinkRequest.link());
            linkRepository.deleteLink(removeLinkRequest.link());
        }
    }

    @KafkaListener(topics = "${kafka.topic.list-links}")
    public void listLinksListen(@Payload String value,
                                @Header(KafkaHeaders.RECEIVED_KEY) Long id) {

        if (!value.equals("GET")) return;

        if (!chatRepository.containChat(id)) {
            log.error("An unsuccessful attempt to get a list of links from a non-existent user with id {}", id);
            throw new NoSuchElementException("Отсутствует чат с таким id");
        }

        log.info("User with id {} requested a list of links", id);

        List<Link> links = chatRepository.getLinks(id);
        kafkaBotService.getLinks(id, new ListLinksResponse(links, links.size()));
    }
}
