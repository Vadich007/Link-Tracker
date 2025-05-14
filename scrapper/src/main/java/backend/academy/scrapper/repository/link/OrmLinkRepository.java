package backend.academy.scrapper.repository.link;

import backend.academy.scrapper.schemas.orm.Links;
import backend.academy.scrapper.schemas.orm.Subscriptions;
import backend.academy.scrapper.schemas.responses.github.Event;
import backend.academy.scrapper.schemas.responses.stackoverflow.Item;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(name = "app.access-type", havingValue = "orm")
@RequiredArgsConstructor
public class OrmLinkRepository implements LinkRepository {
    @PersistenceContext
    private final EntityManager entityManager;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Links findByUrl(String url) {
        try {
            return entityManager
                    .createQuery("SELECT l FROM Links l WHERE l.url = :url", Links.class)
                    .setParameter("url", url)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public boolean containLink(String url) {
        return findByUrl(url) != null;
    }

    @Override
    @Transactional
    public Set<Long> getChats(String url) {
        String jpql = "SELECT s.chatId FROM Links l JOIN l.subscriptions s WHERE l.url = :url";
        return entityManager
                .createQuery(jpql, Long.class)
                .setParameter("url", url)
                .getResultStream()
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional
    public void deleteLink(String url) {
        Links link = findByUrl(url);
        if (link != null) {
            entityManager.remove(link);
        }
    }

    @Override
    @Transactional
    public void addLink(String url) {
        String type = url.startsWith("https://github.com/") ? "gitHub" : "stackOverflow";
        Links link = new Links();
        link.url(url);
        link.type(type);
        entityManager.persist(link);
    }

    @Override
    public Long getLinkId(String url) {
        return findByUrl(url).id();
    }

    @Override
    @Transactional
    public Set<String> getUrls(int limit, int offset) {
        return entityManager
                .createQuery("SELECT l.url FROM Links l", String.class)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultStream()
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional
    public void deleteChat(String url, long chatId) {
        Links link = findByUrl(url);
        for (Subscriptions subscriptions : link.subscriptions()) {
            if (subscriptions.chatId() == chatId) {
                entityManager.remove(subscriptions);
            }
        }
    }

    @Override
    @Transactional
    public void clear() {
        entityManager.createQuery("DELETE FROM Subscriptions").executeUpdate();
        entityManager.createQuery("DELETE FROM Links").executeUpdate();
    }

    @Override
    public int size() {
        return entityManager
                .createQuery("SELECT COUNT(l) FROM Links l", Long.class)
                .getSingleResult()
                .intValue();
    }

    @Override
    @Transactional
    public void setLastEvent(String url, Event event) {
        Links link = findByUrl(url);
        if (link != null) {
            try {
                link.lastEvent(objectMapper.writeValueAsString(event));
                entityManager.merge(link);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to convert JSON to Event", e);
            }
        }
    }

    @Override
    @Transactional
    public void setLastEvent(String url, Item item) {
        Links link = findByUrl(url);
        if (link != null) {
            try {
                link.lastEvent(objectMapper.writeValueAsString(item));
                entityManager.merge(link);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to convert JSON to Item", e);
            }
        }
    }

    @Override
    public boolean isLastEvent(String url, Event event) {
        Links link = findByUrl(url);
        if (link.lastEvent() == null) return false;

        try {
            return objectMapper.readValue(link.lastEvent(), Event.class).id().equals(event.id());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert JSON to Event", e);
        }
    }

    @Override
    public boolean isLastEvent(String url, Long postId) {
        Links link = findByUrl(url);
        if (link.lastEvent() == null) return false;

        try {
            return objectMapper.readValue(link.lastEvent(), Item.class).postId().equals(postId);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert JSON to Item", e);
        }
    }

    @Override
    public Event getLastGitHubEvent(String url) {
        Links link = findByUrl(url);
        try {
            return objectMapper.readValue(link.lastEvent(), Event.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert JSON to Event", e);
        }
    }

    @Override
    public Item getLastStackOverflowEvent(String url) {
        Links link = findByUrl(url);
        try {
            return objectMapper.readValue(link.lastEvent(), Item.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert JSON to Item", e);
        }
    }
}
