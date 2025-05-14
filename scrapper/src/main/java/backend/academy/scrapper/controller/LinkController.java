package backend.academy.scrapper.controller;

import backend.academy.scrapper.repository.chat.ChatRepository;
import backend.academy.scrapper.repository.link.LinkRepository;
import backend.academy.scrapper.schemas.models.Link;
import backend.academy.scrapper.schemas.requests.AddLinkRequest;
import backend.academy.scrapper.schemas.requests.RemoveLinkRequest;
import backend.academy.scrapper.schemas.responses.bot.LinkResponse;
import backend.academy.scrapper.schemas.responses.bot.ListLinksResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/links")
@AllArgsConstructor
@Slf4j
public class LinkController {
    private final ChatRepository chatRepository;
    private final LinkRepository linkRepository;

    @PostMapping
    public LinkResponse addLink(
            @Positive @RequestHeader("Tg-Chat-Id") long id,
            @NotNull @RequestBody @Valid AddLinkRequest addLinkRequest) {
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

        LinkResponse response = new LinkResponse(
                linkRepository.getLinkId(addLinkRequest.link()),
                addLinkRequest.link(),
                addLinkRequest.tags(),
                addLinkRequest.filters());

        Link link = new Link(response.id(), addLinkRequest.link(), addLinkRequest.tags(), addLinkRequest.filters());
        chatRepository.subscribeLink(id, link);
        log.info("User with id {} subscribed to link {}", id, addLinkRequest.link());

        return response;
    }

    @GetMapping
    public ListLinksResponse getLinks(@Positive @RequestHeader("Tg-Chat-Id") long id) {
        if (!chatRepository.containChat(id)) {
            log.error("An unsuccessful attempt to get a list of links from a non-existent user with id {}", id);
            throw new NoSuchElementException("Отсутствует чат с таким id");
        }

        log.info("User with id {} requested a list of links", id);

        List<Link> links = chatRepository.getLinks(id);
        return new ListLinksResponse(links, links.size());
    }

    @DeleteMapping
    public LinkResponse deleteLink(
            @Positive @RequestHeader("Tg-Chat-Id") long id,
            @NotNull @RequestBody @Valid RemoveLinkRequest removeLinkRequest) {
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

        return new LinkResponse(optional.orElseThrow());
    }
}
