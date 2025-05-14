package backend.academy.scrapper.repository.chat;

import backend.academy.scrapper.schemas.models.Link;
import backend.academy.scrapper.schemas.orm.Chats;
import backend.academy.scrapper.schemas.orm.Links;
import backend.academy.scrapper.schemas.orm.Subscriptions;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(name = "app.access-type", havingValue = "orm")
@RequiredArgsConstructor
public class OrmChatRepository implements ChatRepository {
    @PersistenceContext
    private final EntityManager entityManager;

    @Override
    public boolean containChat(Long chatId) {
        return entityManager.find(Chats.class, chatId) != null;
    }

    @Override
    @Transactional
    public List<Link> getLinks(Long chatId) {
        String jpql = "SELECT l FROM Links l JOIN l.subscriptions s WHERE s.chatId = :chatId";
        return entityManager.createQuery(jpql, Links.class).setParameter("chatId", chatId).getResultList().stream()
                .map(Link::convertToLink)
                .toList();
    }

    @Override
    @Transactional
    public void deleteChat(Long chatId) {
        Chats chat = entityManager.find(Chats.class, chatId);
        if (chat != null) {
            entityManager.remove(chat);
        }
    }

    @Override
    @Transactional
    public void addChat(Long chatId) {
        Chats chat = new Chats(chatId);
        entityManager.persist(chat);
    }

    @Override
    @Transactional
    public void subscribeLink(Long chatId, Link link) {
        Links linkEntity = entityManager.find(Links.class, link.id());
        Subscriptions subscriptions = new Subscriptions();
        subscriptions.chatId(chatId);
        subscriptions.link(linkEntity);
        subscriptions.tags(link.tags() != null ? String.join(",", link.tags()) : null);
        subscriptions.filters(link.filters() != null ? String.join(",", link.filters()) : null);

        entityManager.persist(subscriptions);
    }

    @Override
    @Transactional
    public void clear() {
        entityManager.createQuery("DELETE FROM Subscriptions").executeUpdate();
        entityManager.createQuery("DELETE FROM Chats").executeUpdate();
    }

    @Override
    public int size() {
        Long count =
                (Long) entityManager.createQuery("SELECT COUNT(s) FROM Chats s").getSingleResult();
        return count.intValue();
    }
}
