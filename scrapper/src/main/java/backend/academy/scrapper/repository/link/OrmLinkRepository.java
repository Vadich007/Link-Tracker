package backend.academy.scrapper.repository.link;

import backend.academy.scrapper.schemas.models.Link;
import backend.academy.scrapper.schemas.orm.Links;
import backend.academy.scrapper.schemas.responses.github.Event;
import backend.academy.scrapper.schemas.responses.stackoverflow.Item;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(name = "app.access-type", havingValue = "orm")
public interface OrmLinkRepository extends LinkRepository, JpaRepository<Links, Long> {

    ObjectMapper objectMapper = new ObjectMapper();

    Optional<Links> findByUrl(String url);

    @Query("SELECT l FROM Links l JOIN l.subscriptions s WHERE s.chatId = :chatId")
    List<Links> findLinksByChatId(@Param("chatId") Long chatId);

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM Subscriptions s WHERE link_id = :linkId AND chat_id = :chatId", nativeQuery = true)
    void deleteChatQuery(@Param("linkId") long linkId, @Param("chatId") long chatId);

    @Override
    default boolean containLink(String url) {
        return findByUrl(url).isPresent();
    }

    @Override
    @Transactional
    default List<Link> getLinks(Long chatId) {
        return findLinksByChatId(chatId).stream()
            .map(Link::convertToLink)
            .toList();
    }

    @Override
    @Query("SELECT DISTINCT s.chatId FROM Links l JOIN l.subscriptions s WHERE l.url = :url")
    Set<Long> getChats(@Param("url") String url);

    @Override
    @Transactional
    default void deleteLink(String url) {
        findByUrl(url).ifPresent(this::delete);
    }

    @Override
    @Transactional
    default void addLink(String url) {
        String type = url.startsWith("https://github.com/") ? "gitHub" : "stackOverflow";
        Links link = new Links();
        link.url(url);
        link.type(type);
        save(link);
    }

    @Override
    default Long getLinkId(String url) {
        return findByUrl(url).map(Links::id).orElse(null);
    }

    @Override
    @Query("SELECT l.url FROM Links l")
    Set<String> getUrls(@Param("limit") int limit, @Param("offset") int offset);

    @Override
    @Transactional
    default void deleteChat(String url, long chatId) {
        findByUrl(url).stream().map(Links::id).findFirst().ifPresent(aLong -> deleteChatQuery(aLong, chatId));
    }

    @Override
    default int size() {
        return (int) count();
    }

    @Override
    @Transactional
    default void setLastEvent(String url, Event event) {
        findByUrl(url).ifPresent(link -> {
            try {
                link.lastEvent(objectMapper.writeValueAsString(event));
                save(link);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to convert JSON to Event", e);
            }
        });
    }

    @Override
    @Transactional
    default void setLastEvent(String url, Item item) {
        findByUrl(url).ifPresent(link -> {
            try {
                link.lastEvent(objectMapper.writeValueAsString(item));
                save(link);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to convert JSON to Item", e);
            }
        });
    }

    @Override
    default boolean isLastEvent(String url, Event event) {
        return findByUrl(url)
            .map(link -> {
                try {
                    if (link.lastEvent() == null) return false;
                    return objectMapper.readValue(link.lastEvent(), Event.class)
                        .id().equals(event.id());
                } catch (JsonProcessingException e) {
                    throw new RuntimeException("Failed to convert JSON to Event", e);
                }
            })
            .orElse(false);
    }

    @Override
    default boolean isLastEvent(String url, Long postId) {
        return findByUrl(url)
            .map(link -> {
                try {
                    if (link.lastEvent() == null) return false;
                    return objectMapper.readValue(link.lastEvent(), Item.class)
                        .postId().equals(postId);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException("Failed to convert JSON to Item", e);
                }
            })
            .orElse(false);
    }

    @Override
    default Event getLastGitHubEvent(String url) {
        return findByUrl(url)
            .map(link -> {
                try {
                    return objectMapper.readValue(link.lastEvent(), Event.class);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException("Failed to convert JSON to Event", e);
                }
            })
            .orElse(null);
    }

    @Override
    default Item getLastStackOverflowEvent(String url) {
        return findByUrl(url)
            .map(link -> {
                try {
                    return objectMapper.readValue(link.lastEvent(), Item.class);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException("Failed to convert JSON to Item", e);
                }
            })
            .orElse(null);
    }
}
