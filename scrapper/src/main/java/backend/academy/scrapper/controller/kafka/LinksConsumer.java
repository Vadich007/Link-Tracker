package backend.academy.scrapper.controller.kafka;

import backend.academy.scrapper.repository.chat.ChatRepository;
import backend.academy.scrapper.repository.link.LinkRepository;
import backend.academy.scrapper.schemas.models.Link;
import backend.academy.scrapper.schemas.requests.AddLinkRequest;
import backend.academy.scrapper.schemas.requests.RemoveLinkRequest;
import java.util.Optional;
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
@KafkaListener(topics = "${kafka.topic.links}")
public class LinksConsumer {
    private final ChatRepository chatRepository;
    private final LinkRepository linkRepository;

    @KafkaHandler
    public void addLinkListen(@Payload AddLinkRequest addLinkRequest, @Header(KafkaHeaders.RECEIVED_KEY) Long id) {

        if (!chatRepository.containChat(id)) {
            log.error(
                    "Unsuccessful attempt to add a link {} from a non-existent user with an id {}",
                    addLinkRequest.link(),
                    id);
            return;
        }

        if (chatRepository.getLinks(id).stream().anyMatch(l -> l.url().equals(addLinkRequest.link()))) {
            log.error("Unsuccessful attempt to add a link {} user with an id {}", addLinkRequest.link(), id);
            return;
        }

        if (!linkRepository.containLink(addLinkRequest.link())) {
            linkRepository.addLink(addLinkRequest.link());
            log.info("User with id {} add link {} in database", id, addLinkRequest.link());
        }

        Link link = new Link(
                linkRepository.getLinkId(addLinkRequest.link()),
                addLinkRequest.link(),
                addLinkRequest.tags(),
                addLinkRequest.filters());
        chatRepository.subscribeLink(id, link);
        log.info("User with id {} subscribed to link {}", id, addLinkRequest.link());
    }

    @KafkaHandler
    public void removeLinkListen(
            @Payload RemoveLinkRequest removeLinkRequest, @Header(KafkaHeaders.RECEIVED_KEY) Long id) {
        if (!chatRepository.containChat(id)) {
            log.error(
                    "Unsuccessful attempt to delete a link {} for a non-existent user with an id {}",
                    removeLinkRequest.link(),
                    id);
            return;
        }

        Optional<Link> optional = chatRepository.getLinks(id).stream()
                .filter(l -> l.url().equals(removeLinkRequest.link()))
                .findFirst();
        if (optional.isEmpty()) {
            log.error(
                    "User with id {} try to delete link {}, which does not exist in repository",
                    id,
                    removeLinkRequest.link());
            return;
        }

        linkRepository.deleteChat(removeLinkRequest.link(), id);
        log.info("User with id {} unsubscribed from link {}", id, removeLinkRequest.link());

        if (linkRepository.getChats(removeLinkRequest.link()).isEmpty()) {
            log.info("Delete link {} from repository", removeLinkRequest.link());
            linkRepository.deleteLink(removeLinkRequest.link());
        }
    }
}
